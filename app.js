/**
 * Blockora - Core Game Engine
 * Features:
 * - 8x8 Grid with 3D Glossy Jewel Blocks
 * - Unlimited / Endless Gameplay
 * - Unified Drag-and-Drop (Desktop Mouse + Mobile Touch with Finger Offset)
 * - Line Clear Prediction Glow & Multi-Line Combos
 * - Procedural Particle Explosion FX
 * - Sound & BGM Integration
 * - LocalStorage High Score & PWA Support
 */

const GRID_SIZE = 8;

const BLOCK_COLORS = {
  RED:    { name: 'block-red',    hex: '#ff1744' },
  ORANGE: { name: 'block-orange', hex: '#ff6d00' },
  YELLOW: { name: 'block-yellow', hex: '#ffd600' },
  GREEN:  { name: 'block-green',  hex: '#00e676' },
  CYAN:   { name: 'block-cyan',   hex: '#00e5ff' },
  BLUE:   { name: 'block-blue',   hex: '#2979ff' },
  PURPLE: { name: 'block-purple', hex: '#aa00ff' },
  PINK:   { name: 'block-pink',   hex: '#ff2a85' }
};

const COLOR_KEYS = Object.keys(BLOCK_COLORS);

// Presets for shapes matching Blockora's catalog
const SHAPE_TEMPLATES = [
  // 1x1 Dot
  { matrix: [[1]], colors: ['YELLOW', 'CYAN', 'PINK'] },

  // 2x1 & 1x2 Dominoes
  { matrix: [[1, 1]], colors: ['CYAN', 'GREEN'] },
  { matrix: [[1], [1]], colors: ['CYAN', 'GREEN'] },

  // 3x1 & 1x3 Lines
  { matrix: [[1, 1, 1]], colors: ['BLUE', 'ORANGE'] },
  { matrix: [[1], [1], [1]], colors: ['BLUE', 'ORANGE'] },

  // 4x1 & 1x4 Lines
  { matrix: [[1, 1, 1, 1]], colors: ['PURPLE', 'CYAN'] },
  { matrix: [[1], [1], [1], [1]], colors: ['PURPLE', 'CYAN'] },

  // 5x1 & 1x5 Lines
  { matrix: [[1, 1, 1, 1, 1]], colors: ['RED', 'BLUE'] },
  { matrix: [[1], [1], [1], [1], [1]], colors: ['RED', 'BLUE'] },

  // 2x2 Square
  { matrix: [[1, 1], [1, 1]], colors: ['YELLOW', 'ORANGE'] },

  // 3x3 Square
  { matrix: [[1, 1, 1], [1, 1, 1], [1, 1, 1]], colors: ['RED', 'PURPLE'] },

  // 2x2 Corner L (4 orientations)
  { matrix: [[1, 0], [1, 1]], colors: ['GREEN', 'PINK'] },
  { matrix: [[0, 1], [1, 1]], colors: ['GREEN', 'PINK'] },
  { matrix: [[1, 1], [1, 0]], colors: ['GREEN', 'PINK'] },
  { matrix: [[1, 1], [0, 1]], colors: ['GREEN', 'PINK'] },

  // 3x3 Corner L (4 orientations)
  { matrix: [[1, 0, 0], [1, 0, 0], [1, 1, 1]], colors: ['ORANGE', 'CYAN'] },
  { matrix: [[0, 0, 1], [0, 0, 1], [1, 1, 1]], colors: ['ORANGE', 'CYAN'] },
  { matrix: [[1, 1, 1], [1, 0, 0], [1, 0, 0]], colors: ['ORANGE', 'CYAN'] },
  { matrix: [[1, 1, 1], [0, 0, 1], [0, 0, 1]], colors: ['ORANGE', 'CYAN'] },

  // 3x2 L shape (4 orientations)
  { matrix: [[1, 0], [1, 0], [1, 1]], colors: ['BLUE', 'PURPLE'] },
  { matrix: [[0, 1], [0, 1], [1, 1]], colors: ['BLUE', 'PURPLE'] },
  { matrix: [[1, 1, 1], [1, 0, 0]], colors: ['BLUE', 'PURPLE'] },
  { matrix: [[1, 1, 1], [0, 0, 1]], colors: ['BLUE', 'PURPLE'] },

  // T Shapes (4 orientations)
  { matrix: [[1, 1, 1], [0, 1, 0]], colors: ['PURPLE', 'PINK'] },
  { matrix: [[0, 1, 0], [1, 1, 1]], colors: ['PURPLE', 'PINK'] },
  { matrix: [[1, 0], [1, 1], [1, 0]], colors: ['PURPLE', 'PINK'] },
  { matrix: [[0, 1], [1, 1], [0, 1]], colors: ['PURPLE', 'PINK'] },

  // Z & S shapes
  { matrix: [[1, 1, 0], [0, 1, 1]], colors: ['RED', 'GREEN'] },
  { matrix: [[0, 1, 1], [1, 1, 0]], colors: ['GREEN', 'YELLOW'] },
  { matrix: [[1, 0], [1, 1], [0, 1]], colors: ['RED', 'GREEN'] },
  { matrix: [[0, 1], [1, 1], [1, 0]], colors: ['GREEN', 'YELLOW'] }
];

// Storage key for per-device local high score
const STORAGE_KEY_BEST_SCORE = 'blockora_best_score';

function loadLocalBestScore() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY_BEST_SCORE);
    if (saved !== null) {
      const parsed = parseInt(saved, 10);
      return !isNaN(parsed) && parsed > 0 ? parsed : 0;
    }
  } catch (e) {
    // Fallback for environments with disabled localStorage
  }
  return 0;
}

function saveLocalBestScore(score) {
  try {
    localStorage.setItem(STORAGE_KEY_BEST_SCORE, Math.max(0, Math.floor(score)).toString());
  } catch (e) {
    // Graceful fallback
  }
}

class BlockoraGame {
  constructor() {
    this.grid = Array(GRID_SIZE).fill(null).map(() => Array(GRID_SIZE).fill(null));
    this.availablePieces = [null, null, null];
    this.score = 0;
    this.highScore = loadLocalBestScore();
    this.totalLines = 0;
    this.comboCount = 0;
    this.isGameOver = false;

    // Drag state
    this.draggingPieceIndex = -1;
    this.draggedShape = null;
    this.touchOffset = { x: 0, y: -70 }; // Offset so piece floats above user's finger on mobile touch

    // Elements
    this.boardEl = document.getElementById('board');
    this.scoreEl = document.getElementById('score-val');
    this.highScoreEl = document.getElementById('high-score-val');
    this.slotsEl = [
      document.getElementById('slot-0'),
      document.getElementById('slot-1'),
      document.getElementById('slot-2')
    ];
    this.dragProxyEl = document.getElementById('drag-proxy');
    this.comboBannerEl = document.getElementById('combo-banner');
    this.gameOverModalEl = document.getElementById('game-over-modal');
    this.finalScoreEl = document.getElementById('final-score-val');
    this.modalHighScoreEl = document.getElementById('modal-high-score-val');
    this.playAgainBtn = document.getElementById('play-again-btn');
    this.restartBtn = document.getElementById('restart-btn');
    this.soundToggleBtn = document.getElementById('sound-toggle-btn');
    this.musicToggleBtn = document.getElementById('music-toggle-btn');

    // Particle Canvas
    this.canvas = document.getElementById('particle-canvas');
    this.ctx = this.canvas.getContext('2d');
    this.particles = [];

    this.init();
  }

  init() {
    this.resizeCanvas();
    window.addEventListener('resize', () => this.resizeCanvas());

    this.renderBoard();
    this.updateStatsUI();
    this.spawnPieceSet();
    this.setupEventListeners();
    this.startParticleLoop();

    // Register service worker if available
    if ('serviceWorker' in navigator) {
      window.addEventListener('load', () => {
        navigator.serviceWorker.register('./sw.js').catch(err => {
          console.warn('SW registration skipped:', err);
        });
      });
    }
  }

  resizeCanvas() {
    const rect = document.querySelector('.app-container').getBoundingClientRect();
    this.canvas.width = rect.width;
    this.canvas.height = rect.height;
  }

  renderBoard() {
    this.boardEl.innerHTML = '';
    for (let r = 0; r < GRID_SIZE; r++) {
      for (let c = 0; c < GRID_SIZE; c++) {
        const cell = document.createElement('div');
        cell.className = 'cell';
        cell.dataset.row = r;
        cell.dataset.col = c;

        const blockColor = this.grid[r][c];
        if (blockColor) {
          const block = document.createElement('div');
          block.className = `block ${BLOCK_COLORS[blockColor].name}`;
          cell.appendChild(block);
        }
        this.boardEl.appendChild(cell);
      }
    }
  }

  spawnPieceSet() {
    this.availablePieces = [
      this.getRandomShape(),
      this.getRandomShape(),
      this.getRandomShape()
    ];
    this.renderPieceSlots();
    this.checkGameOver();
  }

  getRandomShape() {
    const template = SHAPE_TEMPLATES[Math.floor(Math.random() * SHAPE_TEMPLATES.length)];
    const colorKey = template.colors[Math.floor(Math.random() * template.colors.length)] || COLOR_KEYS[Math.floor(Math.random() * COLOR_KEYS.length)];
    return {
      matrix: template.matrix,
      color: colorKey,
      rows: template.matrix.length,
      cols: template.matrix[0].length,
      blockCount: template.matrix.reduce((sum, r) => sum + r.reduce((s, v) => s + v, 0), 0)
    };
  }

  renderPieceSlots() {
    this.slotsEl.forEach((slot, idx) => {
      slot.innerHTML = '';
      const piece = this.availablePieces[idx];
      if (!piece) {
        slot.classList.add('disabled');
        return;
      }
      slot.classList.remove('disabled');

      const pieceCont = document.createElement('div');
      pieceCont.className = 'piece-container';
      pieceCont.style.gridTemplateColumns = `repeat(${piece.cols}, 1fr)`;

      for (let r = 0; r < piece.rows; r++) {
        for (let c = 0; c < piece.cols; c++) {
          const mini = document.createElement('div');
          mini.className = 'mini-block';
          if (piece.matrix[r][c]) {
            mini.classList.add('block', BLOCK_COLORS[piece.color].name);
          } else {
            mini.style.opacity = '0';
          }
          pieceCont.appendChild(mini);
        }
      }
      slot.appendChild(pieceCont);
    });
  }

  setupEventListeners() {
    // Audio context unlock on first touch/click
    const unlockAudio = () => {
      window.soundEngine.resumeContext();
      window.removeEventListener('pointerdown', unlockAudio);
      window.removeEventListener('touchstart', unlockAudio);
    };
    window.addEventListener('pointerdown', unlockAudio);
    window.addEventListener('touchstart', unlockAudio);

    // Audio & UI Controls
    this.soundToggleBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      window.soundEngine.resumeContext();
      const enabled = window.soundEngine.toggleSfx();
      this.soundToggleBtn.classList.toggle('muted', !enabled);
      window.soundEngine.playClick();
    });

    this.musicToggleBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      window.soundEngine.resumeContext();
      const enabled = window.soundEngine.toggleBgm();
      this.musicToggleBtn.classList.toggle('muted', !enabled);
      window.soundEngine.playClick();
    });

    this.restartBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      window.soundEngine.playClick();
      this.restartGame();
    });

    this.playAgainBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      window.soundEngine.playClick();
      this.restartGame();
    });

    // Pointer-based unified Drag and Drop
    this.slotsEl.forEach((slot, index) => {
      const handleStart = (e) => {
        if (this.isGameOver || !this.availablePieces[index]) return;
        e.preventDefault();
        window.soundEngine.resumeContext();
        window.soundEngine.playPop();

        this.draggingPieceIndex = index;
        this.draggedShape = this.availablePieces[index];
        slot.classList.add('dragging');

        this.showDragProxy(e);
        window.addEventListener('pointermove', this.onPointerMove);
        window.addEventListener('pointerup', this.onPointerUp);
        window.addEventListener('pointercancel', this.onPointerUp);
      };

      slot.addEventListener('pointerdown', handleStart);
    });

    this.onPointerMove = (e) => {
      if (this.draggingPieceIndex === -1) return;
      this.updateDragProxyPosition(e);
      this.updateBoardGhost(e);
    };

    this.onPointerUp = (e) => {
      if (this.draggingPieceIndex === -1) return;
      this.dropPiece(e);
      this.clearGhostHighlights();
      this.hideDragProxy();

      if (this.slotsEl[this.draggingPieceIndex]) {
        this.slotsEl[this.draggingPieceIndex].classList.remove('dragging');
      }

      this.draggingPieceIndex = -1;
      this.draggedShape = null;

      window.removeEventListener('pointermove', this.onPointerMove);
      window.removeEventListener('pointerup', this.onPointerUp);
      window.removeEventListener('pointercancel', this.onPointerUp);
    };
  }

  showDragProxy(e) {
    const shape = this.draggedShape;
    this.dragProxyEl.innerHTML = '';
    const pieceCont = document.createElement('div');
    pieceCont.className = 'piece-container';
    pieceCont.style.gridTemplateColumns = `repeat(${shape.cols}, 1fr)`;

    for (let r = 0; r < shape.rows; r++) {
      for (let c = 0; c < shape.cols; c++) {
        const mini = document.createElement('div');
        mini.className = 'mini-block';
        if (shape.matrix[r][c]) {
          mini.classList.add('block', BLOCK_COLORS[shape.color].name);
        } else {
          mini.style.opacity = '0';
        }
        pieceCont.appendChild(mini);
      }
    }
    this.dragProxyEl.appendChild(pieceCont);
    this.dragProxyEl.style.display = 'block';
    this.updateDragProxyPosition(e);
  }

  updateDragProxyPosition(e) {
    const isTouch = e.pointerType === 'touch';
    const offsetX = 0;
    const offsetY = isTouch ? this.touchOffset.y : 0;

    this.dragProxyEl.style.left = `${e.clientX + offsetX}px`;
    this.dragProxyEl.style.top = `${e.clientY + offsetY}px`;
  }

  hideDragProxy() {
    this.dragProxyEl.style.display = 'none';
    this.dragProxyEl.innerHTML = '';
  }

  getBoardCellAtPointer(e) {
    const isTouch = e.pointerType === 'touch';
    const targetX = e.clientX;
    const targetY = e.clientY + (isTouch ? this.touchOffset.y : 0);

    const boardRect = this.boardEl.getBoundingClientRect();
    if (
      targetX < boardRect.left || targetX > boardRect.right ||
      targetY < boardRect.top || targetY > boardRect.bottom
    ) {
      return null;
    }

    const cellWidth = boardRect.width / GRID_SIZE;
    const cellHeight = boardRect.height / GRID_SIZE;

    // Calculate top-left origin based on piece center
    const pieceCenterRowOffset = Math.floor(this.draggedShape.rows / 2);
    const pieceCenterColOffset = Math.floor(this.draggedShape.cols / 2);

    const col = Math.floor((targetX - boardRect.left) / cellWidth) - pieceCenterColOffset;
    const row = Math.floor((targetY - boardRect.top) / cellHeight) - pieceCenterRowOffset;

    return { row, col };
  }

  canPlacePiece(shape, startRow, startCol) {
    if (startRow < 0 || startCol < 0 || startRow + shape.rows > GRID_SIZE || startCol + shape.cols > GRID_SIZE) {
      return false;
    }

    for (let r = 0; r < shape.rows; r++) {
      for (let c = 0; c < shape.cols; c++) {
        if (shape.matrix[r][c] && this.grid[startRow + r][startCol + c] !== null) {
          return false;
        }
      }
    }
    return true;
  }

  updateBoardGhost(e) {
    this.clearGhostHighlights();
    const cellPos = this.getBoardCellAtPointer(e);
    if (!cellPos) return;

    const { row, col } = cellPos;
    const shape = this.draggedShape;
    const isValid = this.canPlacePiece(shape, row, col);

    for (let r = 0; r < shape.rows; r++) {
      for (let c = 0; c < shape.cols; c++) {
        if (shape.matrix[r][c]) {
          const targetR = row + r;
          const targetC = col + c;
          if (targetR >= 0 && targetR < GRID_SIZE && targetC >= 0 && targetC < GRID_SIZE) {
            const cellEl = this.boardEl.children[targetR * GRID_SIZE + targetC];
            if (cellEl) {
              cellEl.classList.add(isValid ? 'ghost-valid' : 'ghost-invalid');
            }
          }
        }
      }
    }

    // Line clear prediction highlight
    if (isValid) {
      const { fullRows, fullCols } = this.predictClears(shape, row, col);
      fullRows.forEach(r => {
        for (let c = 0; c < GRID_SIZE; c++) {
          const cellEl = this.boardEl.children[r * GRID_SIZE + c];
          if (cellEl) cellEl.classList.add('will-clear');
        }
      });
      fullCols.forEach(c => {
        for (let r = 0; r < GRID_SIZE; r++) {
          const cellEl = this.boardEl.children[r * GRID_SIZE + c];
          if (cellEl) cellEl.classList.add('will-clear');
        }
      });
    }
  }

  predictClears(shape, startRow, startCol) {
    const tempGrid = this.grid.map(row => [...row]);
    for (let r = 0; r < shape.rows; r++) {
      for (let c = 0; c < shape.cols; c++) {
        if (shape.matrix[r][c]) {
          tempGrid[startRow + r][startCol + c] = shape.color;
        }
      }
    }

    const fullRows = [];
    const fullCols = [];

    for (let r = 0; r < GRID_SIZE; r++) {
      if (tempGrid[r].every(c => c !== null)) fullRows.push(r);
    }
    for (let c = 0; c < GRID_SIZE; c++) {
      let isFull = true;
      for (let r = 0; r < GRID_SIZE; r++) {
        if (tempGrid[r][c] === null) {
          isFull = false;
          break;
        }
      }
      if (isFull) fullCols.push(c);
    }

    return { fullRows, fullCols };
  }

  clearGhostHighlights() {
    const cells = this.boardEl.children;
    for (let i = 0; i < cells.length; i++) {
      cells[i].classList.remove('ghost-valid', 'ghost-invalid', 'will-clear');
    }
  }

  dropPiece(e) {
    const cellPos = this.getBoardCellAtPointer(e);
    if (!cellPos) return;

    const { row, col } = cellPos;
    const shape = this.draggedShape;
    const pieceIndex = this.draggingPieceIndex;

    if (!this.canPlacePiece(shape, row, col)) return;

    // 1. Place piece
    for (let r = 0; r < shape.rows; r++) {
      for (let c = 0; c < shape.cols; c++) {
        if (shape.matrix[r][c]) {
          this.grid[row + r][col + c] = shape.color;
        }
      }
    }

    window.soundEngine.playPlace();

    // 2. Consume slot
    this.availablePieces[pieceIndex] = null;
    this.slotsEl[pieceIndex].innerHTML = '';
    this.slotsEl[pieceIndex].classList.add('disabled');

    // 3. Check for full rows and columns
    const fullRows = [];
    const fullCols = [];

    for (let r = 0; r < GRID_SIZE; r++) {
      if (this.grid[r].every(c => c !== null)) fullRows.push(r);
    }
    for (let c = 0; c < GRID_SIZE; c++) {
      let isFull = true;
      for (let r = 0; r < GRID_SIZE; r++) {
        if (this.grid[r][c] === null) {
          isFull = false;
          break;
        }
      }
      if (isFull) fullCols.push(c);
    }

    const linesCleared = fullRows.size !== undefined ? fullRows.size + fullCols.size : fullRows.length + fullCols.length;
    let points = shape.blockCount * 10;

    if (linesCleared > 0) {
      this.comboCount += 1;
      const comboMult = this.comboCount > 1 ? this.comboCount : 1;
      const clearPts = (linesCleared * 100 + (linesCleared - 1) * 50) * comboMult;
      points += clearPts;
      this.totalLines += linesCleared;

      if (this.comboCount > 1) {
        window.soundEngine.playCombo(this.comboCount);
        this.showComboToast(`COMBO x${this.comboCount}!`);
      } else {
        window.soundEngine.playClear(linesCleared);
        if (linesCleared >= 2) {
          this.showComboToast(`${linesCleared}x BLAST!`);
        }
      }

      // Trigger line explosion particles and animations
      this.explodeLines(fullRows, fullCols);

      // Clear cells from data grid
      fullRows.forEach(r => {
        for (let c = 0; c < GRID_SIZE; c++) this.grid[r][c] = null;
      });
      fullCols.forEach(c => {
        for (let r = 0; r < GRID_SIZE; r++) this.grid[r][c] = null;
      });
    } else {
      this.comboCount = 0;
    }

    // Update Score & trigger gentle scale-up animation
    this.addScore(points);
    this.renderBoard();

    // Check if new set needed
    if (this.availablePieces.every(p => p === null)) {
      this.spawnPieceSet();
    } else {
      this.checkGameOver();
    }
  }

  addScore(points) {
    this.score += points;
    if (this.score > this.highScore) {
      this.highScore = this.score;
      saveLocalBestScore(this.highScore);
    }
    this.updateStatsUI(true);
  }

  updateStatsUI(animateScore = false) {
    this.scoreEl.innerText = this.score.toLocaleString();
    this.highScoreEl.innerText = this.highScore.toLocaleString();

    if (animateScore) {
      this.scoreEl.classList.remove('score-bump');
      void this.scoreEl.offsetWidth; // Force reflow
      this.scoreEl.classList.add('score-bump');
    }
  }

  showComboToast(text) {
    this.comboBannerEl.innerText = text;
    this.comboBannerEl.classList.remove('show');
    void this.comboBannerEl.offsetWidth;
    this.comboBannerEl.classList.add('show');
  }

  explodeLines(rows, cols) {
    const boardRect = this.boardEl.getBoundingClientRect();
    const appRect = document.querySelector('.app-container').getBoundingClientRect();
    const cellW = boardRect.width / GRID_SIZE;
    const cellH = boardRect.height / GRID_SIZE;

    const cellsToExplode = new Set();
    rows.forEach(r => {
      for (let c = 0; c < GRID_SIZE; c++) cellsToExplode.add(`${r},${c}`);
    });
    cols.forEach(c => {
      for (let r = 0; r < GRID_SIZE; r++) cellsToExplode.add(`${r},${c}`);
    });

    cellsToExplode.forEach(key => {
      const [r, c] = key.split(',').map(Number);
      const cellCenterX = (boardRect.left - appRect.left) + c * cellW + cellW / 2;
      const cellCenterY = (boardRect.top - appRect.top) + r * cellH + cellH / 2;
      const colorHex = this.grid[r][c] ? BLOCK_COLORS[this.grid[r][c]].hex : '#00e5ff';
      this.createParticleBurst(cellCenterX, cellCenterY, colorHex);
    });
  }

  createParticleBurst(x, y, color) {
    const count = 10;
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 2.5 + Math.random() * 5.5;
      this.particles.push({
        x: x,
        y: y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        alpha: 1.0,
        color: color,
        size: 3 + Math.random() * 4,
        decay: 0.035 + Math.random() * 0.03
      });
    }
  }

  startParticleLoop() {
    const update = () => {
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

      for (let i = this.particles.length - 1; i >= 0; i--) {
        const p = this.particles[i];
        p.x += p.vx;
        p.y += p.vy;
        p.vy += 0.12; // gravity
        p.alpha -= p.decay;

        if (p.alpha <= 0) {
          this.particles.splice(i, 1);
          continue;
        }

        this.ctx.save();
        this.ctx.globalAlpha = p.alpha;
        this.ctx.fillStyle = p.color;
        this.ctx.beginPath();
        this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        this.ctx.fill();
        this.ctx.restore();
      }

      requestAnimationFrame(update);
    };
    requestAnimationFrame(update);
  }

  checkGameOver() {
    const remaining = this.availablePieces.filter(p => p !== null);
    if (remaining.length === 0) return;

    let canFitAny = false;
    for (const shape of remaining) {
      for (let r = 0; r <= GRID_SIZE - shape.rows; r++) {
        for (let c = 0; c <= GRID_SIZE - shape.cols; c++) {
          if (this.canPlacePiece(shape, r, c)) {
            canFitAny = true;
            break;
          }
        }
        if (canFitAny) break;
      }
      if (canFitAny) break;
    }

    if (!canFitAny) {
      this.isGameOver = true;
      window.soundEngine.playGameOver();
      setTimeout(() => this.showGameOverModal(), 400);
    }
  }

  showGameOverModal() {
    this.finalScoreEl.innerText = this.score.toLocaleString();
    this.modalHighScoreEl.innerText = this.highScore.toLocaleString();
    this.gameOverModalEl.classList.add('active');
  }

  restartGame() {
    this.gameOverModalEl.classList.remove('active');
    this.grid = Array(GRID_SIZE).fill(null).map(() => Array(GRID_SIZE).fill(null));
    this.score = 0;
    this.totalLines = 0;
    this.comboCount = 0;
    this.isGameOver = false;
    this.highScore = loadLocalBestScore();
    this.renderBoard();
    this.updateStatsUI(false);
    this.spawnPieceSet();
  }
}

window.addEventListener('DOMContentLoaded', () => {
  window.blockoraGame = new BlockoraGame();
});
