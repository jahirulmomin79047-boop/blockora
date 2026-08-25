/**
 * Blockora Procedural Web Audio Engine
 * Features:
 * - 16-Bar Relaxing Lo-Fi / Casual Puzzle Soundtrack (Electric Piano, Marimba Melody, Bass, Sparkles)
 * - Synthesized Sound Effects: Pick Up Pop, Place Thud, Line Clear Chords, Combo Fanfare, Game Over Cadence, UI Click
 * - Zero external mp3/wav files required - 100% self-contained Web Audio synthesis
 */

class SoundEngine {
  constructor() {
    this.ctx = null;
    this.isBgmEnabled = true;
    this.isSfxEnabled = true;
    this.bgmGainNode = null;
    this.bgmBuffer = null;
    this.bgmSource = null;
    this.isBgmPlaying = false;
    this.isInitialized = false;
  }

  init() {
    if (this.isInitialized) return;
    try {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtx) return;
      this.ctx = new AudioCtx();
      this.bgmGainNode = this.ctx.createGain();
      this.bgmGainNode.gain.setValueAtTime(0.20, this.ctx.currentTime);
      this.bgmGainNode.connect(this.ctx.destination);
      this.isInitialized = true;
      this.precomputeBgmLoop();
    } catch (e) {
      console.warn('AudioContext initialization deferred:', e);
    }
  }

  resumeContext() {
    if (!this.ctx) {
      this.init();
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
    if (this.isBgmEnabled && !this.isBgmPlaying && this.bgmBuffer) {
      this.startMusic();
    }
  }

  precomputeBgmLoop() {
    if (!this.ctx) return;
    const sampleRate = 32000;
    const bpm = 108;
    const beatSec = 60 / bpm;
    const totalBars = 16;
    const beatsPerBar = 4;
    const totalDurationSec = totalBars * beatsPerBar * beatSec;
    const totalSamples = Math.floor(sampleRate * totalDurationSec);

    const mixBuffer = new Float32Array(totalSamples);

    // Note frequencies
    const C2 = 65.41, D2 = 73.42, E2 = 82.41, F2 = 87.31, G2 = 98.00, A2 = 110.00, B2 = 123.47;
    const C3 = 130.81, D3 = 146.83, E3 = 164.81, F3 = 174.61, G3 = 196.00, A3 = 220.00, B3 = 246.94;
    const C4 = 261.63, D4 = 293.66, E4 = 329.63, F4 = 349.23, G4 = 392.00, A4 = 440.00, B4 = 493.88;
    const C5 = 523.25, D5 = 587.33, E5 = 659.25, F5 = 698.46, G5 = 783.99, A5 = 880.00, B5 = 987.77;
    const C6 = 1046.50, E6 = 1318.51, G6 = 1567.98;

    const harmony = [
      { root: C2, chord: [E4, G4, B4, D5] }, // Cmaj9
      { root: C2, chord: [E4, G4, C5, E5] }, // Cmaj
      { root: A2, chord: [C4, E4, G4, B4] }, // Am9
      { root: A2, chord: [C4, E4, A4, C5] }, // Am7
      { root: F2, chord: [A3, C4, E4, G4] }, // Fmaj9
      { root: F2, chord: [A3, C4, F4, A4] }, // Fmaj7
      { root: G2, chord: [C4, D4, F4, G4] }, // Gsus4
      { root: G2, chord: [B3, D4, F4, G4] }, // G7
      { root: E2, chord: [G3, B3, D4, E4] }, // Em7
      { root: A2, chord: [G3, A3, C4, E4] }, // Am7
      { root: D2, chord: [F3, A3, C4, E4] }, // Dm9
      { root: D2, chord: [F3, A3, D4, F4] }, // Dm7
      { root: F2, chord: [A3, C4, E4, G4] }, // Fmaj9
      { root: G2, chord: [C4, D4, G4, B4] }, // G9sus4
      { root: G2, chord: [B3, D4, G4, D5] }, // G7
      { root: C2, chord: [C4, E4, G4, C5] }  // C
    ];

    // 1. Electric Piano Chords
    for (let bar = 0; bar < totalBars; bar++) {
      const { chord } = harmony[bar];
      const startSample = Math.floor(bar * beatsPerBar * beatSec * sampleRate);
      const barDuration = Math.floor(beatsPerBar * beatSec * sampleRate);

      chord.forEach(freq => {
        for (let s = 0; s < barDuration; s++) {
          const idx = (startSample + s) % totalSamples;
          const t = s / sampleRate;
          const progress = s / barDuration;
          const env = Math.sin(progress * Math.PI) * (0.85 + 0.15 * Math.sin(2 * Math.PI * 1.5 * t));
          const voice1 = Math.sin(2 * Math.PI * freq * t) * 0.55;
          const voice2 = Math.sin(2 * Math.PI * (freq * 1.003) * t) * 0.25;
          const voice3 = Math.sin(2 * Math.PI * (freq * 2.0) * t) * 0.12;
          mixBuffer[idx] += (voice1 + voice2 + voice3) * env * 0.20;
        }
      });
    }

    // 2. Bassline
    const bassHits = [0.0, 1.5, 2.0, 3.5];
    for (let bar = 0; bar < totalBars; bar++) {
      const { root } = harmony[bar];
      const barStart = Math.floor(bar * beatsPerBar * beatSec * sampleRate);
      bassHits.forEach(hitBeat => {
        const hitStart = barStart + Math.floor(hitBeat * beatSec * sampleRate);
        const hitLen = Math.floor(beatSec * 1.2 * sampleRate);
        for (let s = 0; s < hitLen; s++) {
          const idx = (hitStart + s) % totalSamples;
          const t = s / sampleRate;
          const progress = s / hitLen;
          const env = Math.exp(-3.8 * progress) * Math.max(0, 1.0 - progress);
          const tone = Math.sin(2 * Math.PI * root * t) * 0.75 + Math.sin(4 * Math.PI * root * t) * 0.25;
          mixBuffer[idx] += tone * env * 0.30;
        }
      });
    }

    // 3. Cheerful Marimba Melody
    const melodyNotes = [
      [0, 0.0, E5], [0, 1.0, G5], [0, 2.0, B5], [0, 3.0, D5],
      [1, 0.0, C5], [1, 1.5, G4], [1, 2.5, A4], [1, 3.5, C5],
      [2, 0.0, E5], [2, 1.0, D5], [2, 2.0, C5], [2, 3.0, B4],
      [3, 0.0, A4], [3, 1.5, C5], [3, 2.5, E5], [3, 3.5, G5],
      [4, 0.0, A5], [4, 1.0, G5], [4, 2.0, E5], [4, 3.0, D5],
      [5, 0.0, C5], [5, 1.5, E5], [5, 2.5, D5], [5, 3.5, C5],
      [6, 0.0, D5], [6, 1.0, E5], [6, 2.0, G5], [6, 3.0, A5],
      [7, 0.0, G5], [7, 2.0, D5], [7, 3.0, E5],
      [8, 0.0, G5], [8, 1.0, E5], [8, 2.0, B4], [8, 3.0, C5],
      [9, 0.0, D5], [9, 1.5, E5], [9, 2.5, G5], [9, 3.5, A5],
      [10, 0.0, F5], [10, 1.0, E5], [10, 2.0, D5], [10, 3.0, C5],
      [11, 0.0, D5], [11, 1.5, F5], [11, 2.5, A5], [11, 3.5, C6],
      [12, 0.0, B5], [12, 1.0, A5], [12, 2.0, G5], [12, 3.0, E5],
      [13, 0.0, D5], [13, 1.5, E5], [13, 2.5, G5], [13, 3.5, B5],
      [14, 0.0, A5], [14, 1.0, B5], [14, 2.0, G5], [14, 3.0, D5],
      [15, 0.0, C5], [15, 2.0, G4], [15, 3.0, C5]
    ];

    melodyNotes.forEach(([bar, beat, freq]) => {
      const noteStart = Math.floor((bar * beatsPerBar + beat) * beatSec * sampleRate);
      const noteDuration = Math.floor(beatSec * 1.5 * sampleRate);
      for (let s = 0; s < noteDuration; s++) {
        const idx = (noteStart + s) % totalSamples;
        const t = s / sampleRate;
        const progress = s / noteDuration;
        const attack = Math.min(1.0, s / (sampleRate * 0.006));
        const decay = Math.exp(-4.5 * progress) * Math.max(0, 1.0 - progress);
        const env = attack * decay;
        const fundamental = Math.sin(2 * Math.PI * freq * t) * 0.65;
        const harmonic2 = Math.sin(2 * Math.PI * (freq * 2.0) * t) * 0.22;
        const transient = Math.sin(2 * Math.PI * (freq * 4.0) * t) * Math.exp(-28 * progress) * 0.15;
        mixBuffer[idx] += (fundamental + harmonic2 + transient) * env * 0.35;
      }
    });

    // 4. Sparkles
    const sparkleNotes = [G5, C6, E6, G6];
    for (let bar = 0; bar < totalBars; bar++) {
      const barStart = Math.floor(bar * beatsPerBar * beatSec * sampleRate);
      for (let step = 0; step < 8; step++) {
        const note = sparkleNotes[step % sparkleNotes.length];
        const stepStart = barStart + Math.floor((step * 0.5) * beatSec * sampleRate);
        const stepLen = Math.floor(beatSec * 0.45 * sampleRate);
        for (let s = 0; s < stepLen; s++) {
          const idx = (stepStart + s) % totalSamples;
          const t = s / sampleRate;
          const progress = s / stepLen;
          const env = Math.exp(-10.0 * progress);
          mixBuffer[idx] += Math.sin(2 * Math.PI * note * t) * env * 0.07;
        }
      }
    }

    // 5. Crossfade boundary
    const xfade = Math.floor(sampleRate * 0.08);
    for (let i = 0; i < xfade; i++) {
      const r = i / xfade;
      const start = i;
      const end = totalSamples - xfade + i;
      const blended = mixBuffer[start] * r + mixBuffer[end] * (1 - r);
      mixBuffer[start] = blended;
      mixBuffer[end] = blended;
    }

    // Create AudioBuffer
    const audioBuf = this.ctx.createBuffer(1, totalSamples, sampleRate);
    const channelData = audioBuf.getChannelData(0);
    for (let i = 0; i < totalSamples; i++) {
      channelData[i] = Math.max(-1, Math.min(1, mixBuffer[i] * 0.85));
    }
    this.bgmBuffer = audioBuf;

    if (this.isBgmEnabled && !this.isBgmPlaying) {
      this.startMusic();
    }
  }

  startMusic() {
    if (!this.isBgmEnabled || !this.ctx || !this.bgmBuffer || this.isBgmPlaying) return;
    try {
      this.bgmSource = this.ctx.createBufferSource();
      this.bgmSource.buffer = this.bgmBuffer;
      this.bgmSource.loop = true;

      // Soft fade in
      this.bgmGainNode.gain.cancelScheduledValues(this.ctx.currentTime);
      this.bgmGainNode.gain.setValueAtTime(0.001, this.ctx.currentTime);
      this.bgmGainNode.gain.exponentialRampToValueAtTime(0.20, this.ctx.currentTime + 1.2);

      this.bgmSource.connect(this.bgmGainNode);
      this.bgmSource.start(0);
      this.isBgmPlaying = true;
    } catch (e) {
      console.warn('Failed to start BGM:', e);
    }
  }

  stopMusic() {
    if (!this.isBgmPlaying || !this.bgmSource || !this.ctx) return;
    try {
      this.bgmGainNode.gain.cancelScheduledValues(this.ctx.currentTime);
      this.bgmGainNode.gain.setValueAtTime(this.bgmGainNode.gain.value, this.ctx.currentTime);
      this.bgmGainNode.gain.exponentialRampToValueAtTime(0.0001, this.ctx.currentTime + 0.4);
      setTimeout(() => {
        if (this.bgmSource) {
          try { this.bgmSource.stop(); } catch (_) {}
          this.bgmSource.disconnect();
          this.bgmSource = null;
        }
        this.isBgmPlaying = false;
      }, 420);
    } catch (e) {
      this.isBgmPlaying = false;
    }
  }

  toggleBgm() {
    this.isBgmEnabled = !this.isBgmEnabled;
    if (this.isBgmEnabled) {
      this.startMusic();
    } else {
      this.stopMusic();
    }
    return this.isBgmEnabled;
  }

  toggleSfx() {
    this.isSfxEnabled = !this.isSfxEnabled;
    return this.isSfxEnabled;
  }

  // --- Sound Effects Synthesis ---

  playPop() {
    if (!this.isSfxEnabled || !this.ctx) return;
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(420, now);
    osc.frequency.exponentialRampToValueAtTime(1050, now + 0.075);
    gain.gain.setValueAtTime(0.35, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.075);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.08);
  }

  playPlace() {
    if (!this.isSfxEnabled || !this.ctx) return;
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(280, now);
    osc.frequency.exponentialRampToValueAtTime(120, now + 0.09);
    gain.gain.setValueAtTime(0.40, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.095);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.1);
  }

  playClear(combo = 1) {
    if (!this.isSfxEnabled || !this.ctx) return;
    const now = this.ctx.currentTime;
    const baseFreqs = [523.25, 659.25, 783.99, 987.77, 1046.50];
    const pitchMult = 1.0 + Math.min(6, combo) * 0.08;

    baseFreqs.forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq * pitchMult, now + idx * 0.025);
      gain.gain.setValueAtTime(0.001, now);
      gain.gain.setValueAtTime(0.20, now + idx * 0.025);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + idx * 0.025 + 0.35);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(now + idx * 0.025);
      osc.stop(now + idx * 0.025 + 0.36);
    });
  }

  playCombo(combo = 2) {
    if (!this.isSfxEnabled || !this.ctx) return;
    const now = this.ctx.currentTime;
    const notes = [523.25, 659.25, 783.99, 1046.50, 1318.51];
    const pitchMult = 1.0 + Math.min(8, combo) * 0.10;

    notes.forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'triangle';
      const startTime = now + idx * 0.04;
      osc.frequency.setValueAtTime(freq * pitchMult, startTime);
      gain.gain.setValueAtTime(0.25, startTime);
      gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.32);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(startTime);
      osc.stop(startTime + 0.33);
    });
  }

  playGameOver() {
    if (!this.isSfxEnabled || !this.ctx) return;
    const now = this.ctx.currentTime;
    const freqs = [440, 392, 349.23, 293.66];
    freqs.forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      const startTime = now + idx * 0.11;
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, startTime);
      gain.gain.setValueAtTime(0.25, startTime);
      gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.35);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(startTime);
      osc.stop(startTime + 0.36);
    });
  }

  playClick() {
    if (!this.isSfxEnabled || !this.ctx) return;
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(920, now);
    gain.gain.setValueAtTime(0.22, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.035);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.04);
  }
}

window.soundEngine = new SoundEngine();
