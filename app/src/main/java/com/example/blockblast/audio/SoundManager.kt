package com.example.blockblast.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundType, Int>()

    private var bgmJob: Job? = null
    private var bgmTrack: AudioTrack? = null
    private var precomputedBgmLoop: ShortArray? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val targetBgmVolume = 0.22f
    @Volatile
    private var currentBgmGain = 0.0f
    @Volatile
    private var desiredBgmGain = 0.0f

    var isSfxEnabled: Boolean = true
    var isBgmEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                startMusic()
            } else {
                stopMusic()
            }
        }

    enum class SoundType {
        PICK_UP,
        PLACE,
        CLEAR_LINE,
        COMBO,
        GAME_OVER,
        CLICK
    }

    init {
        initSoundPool()
        initBgmEngine()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Generate synthesized sound effects into temp WAV files and load into SoundPool
        scope.launch(Dispatchers.IO) {
            try {
                loadSynthesizedSound(SoundType.PICK_UP) { generatePopWav() }
                loadSynthesizedSound(SoundType.PLACE) { generatePlaceWav() }
                loadSynthesizedSound(SoundType.CLEAR_LINE) { generateClearWav() }
                loadSynthesizedSound(SoundType.COMBO) { generateComboWav() }
                loadSynthesizedSound(SoundType.GAME_OVER) { generateGameOverWav() }
                loadSynthesizedSound(SoundType.CLICK) { generateClickWav() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initBgmEngine() {
        scope.launch(Dispatchers.Default) {
            try {
                precomputedBgmLoop = synthesizeCasualSoundtrack()
                if (isBgmEnabled && bgmJob?.isActive != true) {
                    startMusic()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSynthesizedSound(type: SoundType, generator: () -> ByteArray) {
        try {
            val wavData = generator()
            val tempFile = File(context.cacheDir, "snd_${type.name.lowercase()}.wav")
            FileOutputStream(tempFile).use { it.write(wavData) }
            val soundId = soundPool?.load(tempFile.absolutePath, 1) ?: 0
            if (soundId != 0) {
                soundMap[type] = soundId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSound(type: SoundType, volume: Float = 0.85f) {
        if (!isSfxEnabled) return
        val soundId = soundMap[type] ?: return
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun playClearSoundWithCombo(combo: Int) {
        if (!isSfxEnabled) return
        val soundId = if (combo > 1) soundMap[SoundType.COMBO] else soundMap[SoundType.CLEAR_LINE]
        val pitch = (1.0f + (combo.coerceAtMost(6) * 0.10f))
        if (soundId != null && soundId != 0) {
            soundPool?.play(soundId, 0.95f, 0.95f, 2, 0, pitch)
        }
    }

    fun startMusic() {
        if (!isBgmEnabled) return
        desiredBgmGain = targetBgmVolume
        if (bgmJob?.isActive == true) return

        bgmJob = scope.launch(Dispatchers.IO) {
            playSeamlessBgmLoop()
        }
    }

    fun stopMusic() {
        desiredBgmGain = 0.0f
        scope.launch(Dispatchers.IO) {
            // Smooth fade out over ~300ms before pausing/stopping
            var count = 0
            while (currentBgmGain > 0.01f && count < 30) {
                delay(10)
                count++
            }
            if (!isBgmEnabled || desiredBgmGain <= 0.0f) {
                bgmJob?.cancel()
                bgmJob = null
                try {
                    bgmTrack?.pause()
                    bgmTrack?.flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun playSeamlessBgmLoop() {
        // Wait for precomputed loop if still generating
        while (precomputedBgmLoop == null && scope.isActive) {
            delay(50)
        }
        val pcmData = precomputedBgmLoop ?: return

        val sampleRate = 32000
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        try {
            if (bgmTrack == null) {
                bgmTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize * 4)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }

            bgmTrack?.play()
            currentBgmGain = 0.0f // Start from zero for smooth fade in

            val chunkSize = 2048
            val chunk = ShortArray(chunkSize)
            var sampleIndex = 0
            val totalSamples = pcmData.size

            while (scope.isActive && isBgmEnabled) {
                // Fade in / fade out gain smoothing
                if (currentBgmGain < desiredBgmGain) {
                    currentBgmGain = (currentBgmGain + 0.003f).coerceAtMost(desiredBgmGain)
                } else if (currentBgmGain > desiredBgmGain) {
                    currentBgmGain = (currentBgmGain - 0.006f).coerceAtLeast(desiredBgmGain)
                }

                // If fully faded out and music disabled, break
                if (currentBgmGain <= 0.0001f && desiredBgmGain <= 0.0f) {
                    break
                }

                // Fill chunk with scaled PCM samples
                for (i in 0 until chunkSize) {
                    val rawSample = pcmData[sampleIndex]
                    chunk[i] = (rawSample * currentBgmGain).toInt().coerceIn(-32768, 32767).toShort()
                    sampleIndex++
                    if (sampleIndex >= totalSamples) {
                        sampleIndex = 0 // Seamless loop wrap
                    }
                }

                bgmTrack?.write(chunk, 0, chunkSize)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bgmTrack?.pause()
                bgmTrack?.flush()
            } catch (e: Exception) {
                // ignored
            }
        }
    }

    /**
     * Synthesizes a beautiful, relaxing, modern casual puzzle game soundtrack.
     * Key: C Major / A minor Pentatonic & Lydian color.
     * Tempo: 108 BPM (Cheerful, relaxing, melodic).
     * 16 Bars composed of lush electric piano pads, marimba pluck melody, warm bassline,
     * and delicate high-register sparkle arpeggios.
     */
    private fun synthesizeCasualSoundtrack(): ShortArray {
        val sampleRate = 32000
        val bpm = 108.0
        val beatSec = 60.0 / bpm
        val totalBars = 16
        val beatsPerBar = 4
        val totalDurationSec = totalBars * beatsPerBar * beatSec
        val totalSamples = (sampleRate * totalDurationSec).toInt()

        val mixBuffer = FloatArray(totalSamples)

        // Note frequencies in Hz
        val C2 = 65.41; val D2 = 73.42; val E2 = 82.41; val F2 = 87.31; val G2 = 98.00; val A2 = 110.00; val B2 = 123.47
        val C3 = 130.81; val D3 = 146.83; val E3 = 164.81; val F3 = 174.61; val G3 = 196.00; val A3 = 220.00; val B3 = 246.94
        val C4 = 261.63; val D4 = 293.66; val E4 = 329.63; val F4 = 349.23; val G4 = 392.00; val A4 = 440.00; val B4 = 493.88
        val C5 = 523.25; val D5 = 587.33; val E5 = 659.25; val F5 = 698.46; val G5 = 783.99; val A5 = 880.00; val B5 = 987.77
        val C6 = 1046.50; val E6 = 1318.51; val G6 = 1567.98

        // 16-Bar Chord Progression definitions (Chords & Bass Roots)
        data class BarHarmony(val root: Double, val chordNotes: List<Double>)
        val harmony = listOf(
            // Section A: Cheerful, expansive, relaxing
            BarHarmony(C2, listOf(E4, G4, B4, D5)),         // Bar 1: Cmaj9
            BarHarmony(C2, listOf(E4, G4, C5, E5)),         // Bar 2: Cmaj
            BarHarmony(A2, listOf(C4, E4, G4, B4)),         // Bar 3: Am9
            BarHarmony(A2, listOf(C4, E4, A4, C5)),         // Bar 4: Am7
            BarHarmony(F2, listOf(A3, C4, E4, G4)),         // Bar 5: Fmaj9
            BarHarmony(F2, listOf(A3, C4, F4, A4)),         // Bar 6: Fmaj7
            BarHarmony(G2, listOf(C4, D4, F4, G4)),         // Bar 7: Gsus4
            BarHarmony(G2, listOf(B3, D4, F4, G4)),         // Bar 8: G7

            // Section B: Dynamic, playful and uplifting
            BarHarmony(E2, listOf(G3, B3, D4, E4)),         // Bar 9: Em7
            BarHarmony(A2, listOf(G3, A3, C4, E4)),         // Bar 10: Am7
            BarHarmony(D2, listOf(F3, A3, C4, E4)),         // Bar 11: Dm9
            BarHarmony(D2, listOf(F3, A3, D4, F4)),         // Bar 12: Dm7
            BarHarmony(F2, listOf(A3, C4, E4, G4)),         // Bar 13: Fmaj9
            BarHarmony(G2, listOf(C4, D4, G4, B4)),         // Bar 14: G9sus4
            BarHarmony(G2, listOf(B3, D4, G4, D5)),         // Bar 15: G7
            BarHarmony(C2, listOf(C4, E4, G4, C5))          // Bar 16: C (Seamless resolution)
        )

        // 1. Synthesize Warm Electric Piano Pad / Chords Layer
        for (barIdx in 0 until totalBars) {
            val barData = harmony[barIdx]
            val barStartSample = (barIdx * beatsPerBar * beatSec * sampleRate).toInt()
            val barDurationSamples = (beatsPerBar * beatSec * sampleRate).toInt()

            for (noteFreq in barData.chordNotes) {
                for (s in 0 until barDurationSamples) {
                    val sampleIdx = (barStartSample + s) % totalSamples
                    val t = s.toDouble() / sampleRate
                    val progress = s.toDouble() / barDurationSamples

                    // Smooth swell and sustain envelope
                    val env = sin(progress * PI).coerceIn(0.0, 1.0) * (0.85 + 0.15 * sin(2.0 * PI * 1.5 * t))
                    // Warm harmonic blend with subtle soft detuning for analog shimmer
                    val voice1 = sin(2.0 * PI * noteFreq * t) * 0.55
                    val voice2 = sin(2.0 * PI * (noteFreq * 1.003) * t) * 0.25
                    val voice3 = sin(2.0 * PI * (noteFreq * 2.0) * t) * 0.12

                    val sampleVal = (voice1 + voice2 + voice3) * env * 0.22
                    mixBuffer[sampleIdx] += sampleVal.toFloat()
                }
            }
        }

        // 2. Synthesize Warm Rounded Bassline Layer
        for (barIdx in 0 until totalBars) {
            val rootFreq = harmony[barIdx].root
            val barStartSample = (barIdx * beatsPerBar * beatSec * sampleRate).toInt()
            // Bass hits on beat 1, beat 2.5, beat 3, beat 4.5
            val bassHits = listOf(0.0, 1.5, 2.0, 3.5)

            for (hitBeat in bassHits) {
                val hitStartSample = barStartSample + (hitBeat * beatSec * sampleRate).toInt()
                val hitDurationSamples = (beatSec * 1.2 * sampleRate).toInt()

                for (s in 0 until hitDurationSamples) {
                    val sampleIdx = (hitStartSample + s) % totalSamples
                    val t = s.toDouble() / sampleRate
                    val progress = s.toDouble() / hitDurationSamples

                    val env = exp(-3.8 * progress) * (1.0 - progress).coerceAtLeast(0.0)
                    val tone = sin(2.0 * PI * rootFreq * t) * 0.75 + sin(4.0 * PI * rootFreq * t) * 0.25
                    val sampleVal = tone * env * 0.32
                    mixBuffer[sampleIdx] += sampleVal.toFloat()
                }
            }
        }

        // 3. Synthesize Cheerful & Playful Marimba / Pluck Lead Melody Layer
        // Catchy casual puzzle melody phrases (bar, beat, noteFreq, durationInBeats)
        val melodyNotes = listOf(
            // Section A: Melodic opening phrase
            Triple(0, 0.0, E5), Triple(0, 1.0, G5), Triple(0, 2.0, B5), Triple(0, 3.0, D5),
            Triple(1, 0.0, C5), Triple(1, 1.5, G4), Triple(1, 2.5, A4), Triple(1, 3.5, C5),
            Triple(2, 0.0, E5), Triple(2, 1.0, D5), Triple(2, 2.0, C5), Triple(2, 3.0, B4),
            Triple(3, 0.0, A4), Triple(3, 1.5, C5), Triple(3, 2.5, E5), Triple(3, 3.5, G5),
            Triple(4, 0.0, A5), Triple(4, 1.0, G5), Triple(4, 2.0, E5), Triple(4, 3.0, D5),
            Triple(5, 0.0, C5), Triple(5, 1.5, E5), Triple(5, 2.5, D5), Triple(5, 3.5, C5),
            Triple(6, 0.0, D5), Triple(6, 1.0, E5), Triple(6, 2.0, G5), Triple(6, 3.0, A5),
            Triple(7, 0.0, G5), Triple(7, 2.0, D5), Triple(7, 3.0, E5),

            // Section B: Cheerful variation & resolution
            Triple(8, 0.0, G5), Triple(8, 1.0, E5), Triple(8, 2.0, B4), Triple(8, 3.0, C5),
            Triple(9, 0.0, D5), Triple(9, 1.5, E5), Triple(9, 2.5, G5), Triple(9, 3.5, A5),
            Triple(10, 0.0, F5), Triple(10, 1.0, E5), Triple(10, 2.0, D5), Triple(10, 3.0, C5),
            Triple(11, 0.0, D5), Triple(11, 1.5, F5), Triple(11, 2.5, A5), Triple(11, 3.5, C6),
            Triple(12, 0.0, B5), Triple(12, 1.0, A5), Triple(12, 2.0, G5), Triple(12, 3.0, E5),
            Triple(13, 0.0, D5), Triple(13, 1.5, E5), Triple(13, 2.5, G5), Triple(13, 3.5, B5),
            Triple(14, 0.0, A5), Triple(14, 1.0, B5), Triple(14, 2.0, G5), Triple(14, 3.0, D5),
            Triple(15, 0.0, C5), Triple(15, 2.0, G4), Triple(15, 3.0, C5)
        )

        for ((bar, beat, freq) in melodyNotes) {
            val noteStartSample = ((bar * beatsPerBar + beat) * beatSec * sampleRate).toInt()
            val noteDurationSamples = (beatSec * 1.6 * sampleRate).toInt()

            for (s in 0 until noteDurationSamples) {
                val sampleIdx = (noteStartSample + s) % totalSamples
                val t = s.toDouble() / sampleRate
                val progress = s.toDouble() / noteDurationSamples

                // Pluck envelope: snappy marimba attack + bell resonance
                val attack = (s.toDouble() / (sampleRate * 0.006)).coerceIn(0.0, 1.0)
                val decay = exp(-4.5 * progress) * (1.0 - progress).coerceAtLeast(0.0)
                val env = attack * decay

                // Marimba & bell composite timbre
                val fundamental = sin(2.0 * PI * freq * t) * 0.65
                val harmonic2 = sin(2.0 * PI * (freq * 2.0) * t) * 0.22
                val harmonic3 = sin(2.0 * PI * (freq * 3.0) * t) * 0.08
                val marimbaTransient = sin(2.0 * PI * (freq * 4.0) * t) * exp(-28.0 * progress) * 0.15

                val sampleVal = (fundamental + harmonic2 + harmonic3 + marimbaTransient) * env * 0.38
                mixBuffer[sampleIdx] += sampleVal.toFloat()
            }
        }

        // 4. Synthesize Delicate High Sparkle Arpeggio Layer (adds magical casual puzzle charm)
        val sparkleNotes = listOf(G5, C6, E6, G6)
        for (barIdx in 0 until totalBars) {
            val barStartSample = (barIdx * beatsPerBar * beatSec * sampleRate).toInt()
            for (step in 0 until 8) { // 8th note sparkle pulses
                val note = sparkleNotes[step % sparkleNotes.size]
                val stepStartSample = barStartSample + ((step * 0.5) * beatSec * sampleRate).toInt()
                val stepDurationSamples = (beatSec * 0.45 * sampleRate).toInt()

                for (s in 0 until stepDurationSamples) {
                    val sampleIdx = (stepStartSample + s) % totalSamples
                    val t = s.toDouble() / sampleRate
                    val progress = s.toDouble() / stepDurationSamples

                    val env = exp(-10.0 * progress)
                    val sparkleSample = sin(2.0 * PI * note * t) * env * 0.08
                    mixBuffer[sampleIdx] += sparkleSample.toFloat()
                }
            }
        }

        // 5. Crossfade boundary smoothing to ensure 100% gapless and click-free infinite loop
        val crossfadeSamples = (sampleRate * 0.08).toInt() // 80ms crossfade at loop boundary
        for (i in 0 until crossfadeSamples) {
            val fadeRatio = i.toDouble() / crossfadeSamples
            val startIdx = i
            val endIdx = totalSamples - crossfadeSamples + i
            val blended = (mixBuffer[startIdx] * fadeRatio + mixBuffer[endIdx] * (1.0 - fadeRatio)).toFloat()
            mixBuffer[startIdx] = blended
            mixBuffer[endIdx] = blended
        }

        // Convert normalized FloatArray mix into 16-bit PCM ShortArray
        val finalPcm = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            finalPcm[i] = (mixBuffer[i] * Short.MAX_VALUE * 0.90f).toInt().coerceIn(-32768, 32767).toShort()
        }

        return finalPcm
    }

    fun release() {
        stopMusic()
        soundPool?.release()
        soundPool = null
    }

    // --- Sound Effects Synthesis WAV Generators ---

    private fun generatePopWav(): ByteArray {
        val sampleRate = 32000
        val duration = 0.075
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / samples
            // Crisply ascending bubble/marimba pop
            val freq = 420.0 + (progress * 680.0)
            val env = exp(-6.5 * progress) * (1.0 - progress)
            val s = sin(2.0 * PI * freq * t) * 0.85 + sin(4.0 * PI * freq * t) * 0.15
            data[i] = (s * env * Short.MAX_VALUE * 0.65).toInt().toShort()
        }
        return createWavFile(data, sampleRate)
    }

    private fun generatePlaceWav(): ByteArray {
        val sampleRate = 32000
        val duration = 0.095
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / samples
            // Satisfying solid thud-snap
            val freq = 280.0 - (progress * 130.0)
            val env = exp(-9.0 * progress) * (1.0 - progress)
            val s = (sin(2.0 * PI * freq * t) * 0.75 + sin(2.0 * PI * (freq * 0.5) * t) * 0.35) * env
            data[i] = (s * Short.MAX_VALUE * 0.75).toInt().toShort()
        }
        return createWavFile(data, sampleRate)
    }

    private fun generateClearWav(): ByteArray {
        val sampleRate = 32000
        val duration = 0.38
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / samples
            val env = exp(-3.2 * progress) * (1.0 - progress)
            // Sparkling crystalline chord: C5 + E5 + G5 + B5 + C6
            val s = (sin(2.0 * PI * 523.25 * t) * 0.35 +
                    sin(2.0 * PI * 659.25 * t) * 0.30 +
                    sin(2.0 * PI * 783.99 * t) * 0.25 +
                    sin(2.0 * PI * 987.77 * t) * 0.15 +
                    sin(2.0 * PI * 1046.50 * t) * 0.15) * env
            data[i] = (s * Short.MAX_VALUE * 0.85).toInt().toShort()
        }
        return createWavFile(data, sampleRate)
    }

    private fun generateComboWav(): ByteArray {
        val sampleRate = 32000
        val duration = 0.48
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / samples
            val env = exp(-2.8 * progress) * (1.0 - progress)
            // Grand celebratory fanfare sweep: C5 -> E5 -> G5 -> C6
            val freq = 523.25 + (progress * 780.0)
            val s = (sin(2.0 * PI * freq * t) * 0.55 +
                    sin(2.0 * PI * (freq * 1.25) * t) * 0.30 +
                    sin(2.0 * PI * (freq * 1.5) * t) * 0.25) * env
            data[i] = (s * Short.MAX_VALUE * 0.90).toInt().toShort()
        }
        return createWavFile(data, sampleRate)
    }

    private fun generateGameOverWav(): ByteArray {
        val sampleRate = 32000
        val duration = 0.55
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / samples
            val env = (1.0 - progress) * exp(-2.0 * progress)
            // Gentle melancholic cadence
            val freq = 440.0 - (progress * 220.0)
            val s = (sin(2.0 * PI * freq * t) * 0.7 + sin(2.0 * PI * (freq * 0.5) * t) * 0.3) * env
            data[i] = (s * Short.MAX_VALUE * 0.70).toInt().toShort()
        }
        return createWavFile(data, sampleRate)
    }

    private fun generateClickWav(): ByteArray {
        val sampleRate = 32000
        val duration = 0.035
        val samples = (sampleRate * duration).toInt()
        val data = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / samples
            val env = 1.0 - progress
            val s = sin(2.0 * PI * 920.0 * t) * env
            data[i] = (s * Short.MAX_VALUE * 0.45).toInt().toShort()
        }
        return createWavFile(data, sampleRate)
    }

    private fun createWavFile(data: ShortArray, sampleRate: Int): ByteArray {
        val byteData = ByteArray(data.size * 2)
        for (i in data.indices) {
            byteData[i * 2] = (data[i].toInt() and 0xff).toByte()
            byteData[i * 2 + 1] = ((data[i].toInt() shr 8) and 0xff).toByte()
        }

        val totalDataLen = byteData.size + 36
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // 1 for PCM format
        header[21] = 0
        header[22] = 1 // 1 channel
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        val byteRate = sampleRate * 2
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        val dataLen = byteData.size
        header[40] = (dataLen and 0xff).toByte()
        header[41] = ((dataLen shr 8) and 0xff).toByte()
        header[42] = ((dataLen shr 16) and 0xff).toByte()
        header[43] = ((dataLen shr 24) and 0xff).toByte()

        return header + byteData
    }
}
