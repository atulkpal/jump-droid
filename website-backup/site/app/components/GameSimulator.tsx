"use client";

import { useEffect, useRef, useState } from "react";
import { motion } from "framer-motion";

// Procedural Platform definition
interface Platform {
  x: number;
  y: number;
  width: number;
  height: number;
  type: "NORMAL" | "MOVING" | "ICE" | "BOOST" | "BREAKABLE";
  direction?: number;
  shattered?: boolean;
  shatterTimer?: number;
}

// Spark particle definition
interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  color: string;
  size: number;
  life: number;
  maxLife: number;
}

// Self-contained Web Audio Sound Manager
class SoundSynth {
  private ctx: AudioContext | null = null;

  private init() {
    if (this.ctx) {
      if (this.ctx.state === "suspended") {
        this.ctx.resume().catch(() => {});
      }
      return;
    }
    try {
      const AudioCtx = (window as any).AudioContext || (window as any).webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
        if (this.ctx && this.ctx.state === "suspended") {
          this.ctx.resume().catch(() => {});
        }
      }
    } catch (e) {
      console.warn("Web Audio API is not supported in this browser:", e);
    }
  }

  playThrust(active: boolean) {
    if (!active) return;
    try {
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = "triangle";
      osc.frequency.setValueAtTime(55, this.ctx.currentTime); // Low bass frequency
      gain.gain.setValueAtTime(0.08, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, this.ctx.currentTime + 0.1);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.1);
    } catch (e) {
      console.warn("Error playing thrust sound:", e);
    }
  }

  playLand() {
    try {
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(330, this.ctx.currentTime); // E4
      osc.frequency.exponentialRampToValueAtTime(440, this.ctx.currentTime + 0.08); // A4
      gain.gain.setValueAtTime(0.05, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.1);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.1);
    } catch (e) {
      console.warn("Error playing landing sound:", e);
    }
  }

  playBoost() {
    try {
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = "sawtooth";
      osc.frequency.setValueAtTime(440, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(880, this.ctx.currentTime + 0.2);
      gain.gain.setValueAtTime(0.06, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.2);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.2);
    } catch (e) {
      console.warn("Error playing boost sound:", e);
    }
  }

  playOverheat() {
    try {
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = "sawtooth";
      osc.frequency.setValueAtTime(220, this.ctx.currentTime);
      osc.frequency.linearRampToValueAtTime(80, this.ctx.currentTime + 0.4);
      gain.gain.setValueAtTime(0.1, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.4);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.4);
    } catch (e) {
      console.warn("Error playing overheat sound:", e);
    }
  }

  playExplode() {
    try {
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = "triangle";
      osc.frequency.setValueAtTime(100, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(30, this.ctx.currentTime + 0.5);
      gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.5);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.5);
    } catch (e) {
      console.warn("Error playing explode sound:", e);
    }
  }
}

export default function GameSimulator() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [gameOver, setGameOver] = useState(false);
  const [score, setScore] = useState(0);
  const [maxScore, setMaxScore] = useState(0);
  const [fuel, setFuel] = useState(100);
  const [heat, setHeat] = useState(0);
  const [shield, setShield] = useState(100);
  const [activeZone, setActiveZone] = useState("Earth");
  const soundRef = useRef<SoundSynth | null>(null);

  // Keyboard controls state
  const keys = useRef<{ Left: boolean; Right: boolean; Space: boolean }>({
    Left: false,
    Right: false,
    Space: false,
  });

  // Mouse / touch coordinate target state
  const mouseTargetX = useRef<number | null>(null);
  const isThrustingMouse = useRef(false);

  useEffect(() => {
    soundRef.current = new SoundSynth();
    try {
      const storedMax = localStorage.getItem("sim-max-score");
      if (storedMax) {
        setMaxScore(parseInt(storedMax, 10));
      }
    } catch (e) {
      console.warn("localStorage not available", e);
    }
  }, []);

  const startGame = () => {
    setIsPlaying(true);
    setGameOver(false);
    setScore(0);
    setFuel(100);
    setHeat(0);
    setShield(100);
    setActiveZone("Earth");
    if (soundRef.current) {
      soundRef.current.playBoost();
    }
  };

  useEffect(() => {
    // Keyboard listeners
    const handleKeyDown = (e: KeyboardEvent) => {
      if (["ArrowLeft", "KeyA"].includes(e.code)) keys.current.Left = true;
      if (["ArrowRight", "KeyD"].includes(e.code)) keys.current.Right = true;
      if (e.code === "Space") {
        keys.current.Space = true;
        e.preventDefault();
      }
    };

    const handleKeyUp = (e: KeyboardEvent) => {
      if (["ArrowLeft", "KeyA"].includes(e.code)) keys.current.Left = false;
      if (["ArrowRight", "KeyD"].includes(e.code)) keys.current.Right = false;
      if (e.code === "Space") keys.current.Space = false;
    };

    window.addEventListener("keydown", handleKeyDown);
    window.addEventListener("keyup", handleKeyUp);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      window.removeEventListener("keyup", handleKeyUp);
    };
  }, []);

  useEffect(() => {
    if (!isPlaying || gameOver) return;

    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    // Viewport and physics configs
    const viewW = 480;
    const viewH = 640;
    canvas.width = viewW;
    canvas.height = viewH;

    // Player (Droid) state
    let player = {
      x: viewW / 2,
      y: viewH - 120,
      radius: 12,
      vx: 0,
      vy: 0,
      fuel: 100,
      heat: 0,
      shield: 100,
      overheated: false,
      overheatTimer: 0,
    };

    // Camera offset (climb height)
    let cameraY = 0;
    let localMaxHeight = player.y;

    // Procedural platforms list
    let platforms: Platform[] = [
      { x: viewW / 2 - 50, y: viewH - 50, width: 100, height: 10, type: "NORMAL" },
    ];

    // Particle pool
    let particles: Particle[] = [];

    // Steer & Movement logic
    const GRAVITY = 1200;
    const THRUST = 2800;
    const AIR_RESIST = 0.98;

    const spawnPlatform = (y: number) => {
      const types: Platform["type"][] = ["NORMAL", "MOVING", "ICE", "BOOST", "BREAKABLE"];
      // Random type weighted by altitude
      let type: Platform["type"] = "NORMAL";
      const roll = Math.random();

      if (y < -3000) {
        // High altitude (deep space / orbit equivalent)
        if (roll > 0.8) type = "BOOST";
        else if (roll > 0.5) type = "ICE";
        else if (roll > 0.3) type = "BREAKABLE";
        else type = "MOVING";
      } else if (y < -1000) {
        // Clouds Layer equivalent
        if (roll > 0.8) type = "BOOST";
        else if (roll > 0.6) type = "ICE";
        else if (roll > 0.4) type = "MOVING";
      } else {
        // Early Earth biome
        if (roll > 0.8) type = "MOVING";
      }

      const pW = 80 - Math.min(30, Math.floor(Math.abs(y) / 200)); // Platforms narrow as you go higher
      const pX = Math.random() * (viewW - pW - 40) + 20;
      platforms.push({
        x: pX,
        y: y,
        width: pW,
        height: 10,
        type: type,
        direction: Math.random() > 0.5 ? 1 : -1,
      });
    };

    // Pre-populate starting platforms
    for (let currentY = viewH - 180; currentY > -300; currentY -= 120) {
      spawnPlatform(currentY);
    }

    let lastTime = performance.now();
    let animationId: number;

    const gameLoop = (time: number) => {
      const now = time || performance.now();
      const dt = Math.min((now - lastTime) / 1000, 0.1);
      lastTime = now;

      // --- 1. Physics & Updates ---
      
      // Horizontal control
      let steerInput = 0;
      if (keys.current.Left) steerInput = -1;
      if (keys.current.Right) steerInput = 1;

      // Mouse/touch target steering
      if (mouseTargetX.current !== null) {
        const dx = mouseTargetX.current - player.x;
        if (Math.abs(dx) > 10) {
          steerInput = dx > 0 ? 1 : -1;
        }
      }

      // Check slippery ice physics
      const currentPlatformIndex = platforms.findIndex(
        (p) =>
          !p.shattered &&
          Math.abs(player.y + player.radius - p.y) <= 8 &&
          player.x + player.radius / 2 >= p.x &&
          player.x - player.radius / 2 <= p.x + p.width
      );

      const isOnIce = currentPlatformIndex !== -1 && platforms[currentPlatformIndex].type === "ICE";
      const horizontalAcc = isOnIce ? 600 : 2500;
      const friction = isOnIce ? 0.992 : 0.82;

      player.vx += steerInput * horizontalAcc * dt;
      player.vx *= friction;
      player.x += player.vx * dt;

      // Screen wrap horizontal bounds
      if (player.x < 0) player.x = viewW;
      if (player.x > viewW) player.x = 0;

      // Handle Overheat mechanics
      if (player.overheated) {
        player.overheatTimer -= dt;
        player.heat = Math.max(0, (player.overheatTimer / 2.0) * 100);
        if (player.overheatTimer <= 0) {
          player.overheated = false;
        }
      } else {
        // Cooling rate
        if (!keys.current.Space && !isThrustingMouse.current) {
          player.heat = Math.max(0, player.heat - 24 * dt);
        }
      }

      // Vertical thruster logic
      const isThrusting = (keys.current.Space || isThrustingMouse.current) && player.fuel > 0 && !player.overheated;
      
      if (isThrusting) {
        player.vy -= THRUST * dt;
        player.fuel = Math.max(0, player.fuel - 28 * dt);
        player.heat = Math.min(100, player.heat + 35 * dt);

        if (soundRef.current && Math.random() > 0.6) {
          soundRef.current.playThrust(true);
        }

        // Trigger heat warning (Overheat)
        if (player.heat >= 100) {
          player.overheated = true;
          player.overheatTimer = 2.0;
          if (soundRef.current) soundRef.current.playOverheat();
        }

        // Add flame particles
        if (Math.random() > 0.2) {
          particles.push({
            x: player.x + (Math.random() - 0.5) * 8,
            y: player.y + player.radius + 2,
            vx: player.vx * 0.4 + (Math.random() - 0.5) * 30,
            vy: player.vy * 0.1 + 80 + Math.random() * 80,
            color: player.heat > 80 ? "#FF1744" : "#00E5FF",
            size: Math.random() * 3 + 2,
            life: 0.3,
            maxLife: 0.3,
          });
        }
      }

      // Gravity and air drag
      player.vy += GRAVITY * dt;
      player.vy *= AIR_RESIST;
      player.y += player.vy * dt;

      // Platform collisions (only on downway descent)
      if (player.vy >= 0) {
        platforms.forEach((p) => {
          if (p.shattered) return;
          // Robust line-crossing intersection check to prevent tunneling
          const prevBottom = player.y - player.vy * dt + player.radius;
          const currBottom = player.y + player.radius;
          if (
            prevBottom <= p.y + 4 &&
            currBottom >= p.y &&
            player.x + player.radius / 2 >= p.x &&
            player.x - player.radius / 2 <= p.x + p.width
          ) {
            // Touchdown!
            player.y = p.y - player.radius;
            
            if (p.type === "BOOST") {
              player.vy = -1100;
              player.fuel = 100;
              if (soundRef.current) soundRef.current.playBoost();
            } else if (p.type === "BREAKABLE") {
              p.shattered = true;
              p.shatterTimer = 0.4; // Starts cracking
              player.vy = -450;
              player.fuel = Math.min(100, player.fuel + 40);
              if (soundRef.current) soundRef.current.playLand();
            } else {
              // Standard or moving platform bounce
              player.vy = -450;
              player.fuel = 100; // recharge fuel
              if (soundRef.current) soundRef.current.playLand();
            }

            // Spark effects on landing
            for (let k = 0; k < 6; k++) {
              particles.push({
                x: player.x,
                y: p.y,
                vx: (Math.random() - 0.5) * 150,
                vy: -Math.random() * 80 - 40,
                color: p.type === "BOOST" ? "#FFD700" : "#FFFFFF",
                size: Math.random() * 2 + 1,
                life: 0.4,
                maxLife: 0.4,
              });
            }
          }
        });
      }

      // Handle breakable platforms shattering timer
      platforms.forEach((p) => {
        if (p.shatterTimer !== undefined) {
          p.shatterTimer -= dt;
          if (p.shatterTimer <= 0) {
            // Remove or visually disable
            p.y = 9999; 
          }
        }
      });

      // Platform movement
      platforms.forEach((p) => {
        if (p.type === "MOVING") {
          const speed = 70 + Math.min(50, Math.floor(Math.abs(p.y) / 400));
          p.x += (p.direction || 1) * speed * dt;
          if (p.x <= 10 || p.x + p.width >= viewW - 10) {
            p.direction = -(p.direction || 1);
          }
        }
      });

      // Update particles
      particles.forEach((part) => {
        part.life -= dt;
        part.x += part.vx * dt;
        part.y += part.vy * dt;
      });
      particles = particles.filter((part) => part.life > 0);

      // Camera follow logic
      if (player.y < localMaxHeight) {
        localMaxHeight = player.y;
      }
      // Target camera center
      const targetCamY = viewH - 240 - localMaxHeight;
      cameraY += (targetCamY - cameraY) * 4 * dt;

      // Procedural generation threshold: Keep platforms spawning above camera view
      const topVisibleY = -cameraY;
      let lowestPlatformY = -99999;
      platforms.forEach((p) => {
        if (p.y > lowestPlatformY && p.y !== 9999) lowestPlatformY = p.y;
      });

      // Spawn next platform if there's space
      let highestPlatformY = 99999;
      platforms.forEach((p) => {
        if (p.y < highestPlatformY && p.y !== 9999) highestPlatformY = p.y;
      });

      if (highestPlatformY > topVisibleY - 200) {
        spawnPlatform(highestPlatformY - 130);
      }

      // Cleanup low off-screen platforms
      platforms = platforms.filter((p) => p.y < -cameraY + viewH + 200);

      // Calculate altitude score (1px climb = 0.5 meters)
      const currentScore = Math.max(0, Math.floor((-localMaxHeight + (viewH - 120)) * 0.4));

      // Biome transition logic based on altitude
      let zone = "Earth";
      if (currentScore > 3500) zone = "Orbit";
      else if (currentScore > 1200) zone = "Cloud Layer";

      // Direct DOM updates for butter-smooth 60fps performance
      const elScore = document.getElementById("sim-score");
      if (elScore) elScore.innerText = `${currentScore}m`;

      const elZone = document.getElementById("sim-zone");
      if (elZone) elZone.innerText = zone;

      const elFuelBar = document.getElementById("sim-fuel-bar");
      const elFuelVal = document.getElementById("sim-fuel-val");
      if (elFuelBar) elFuelBar.style.width = `${player.fuel}%`;
      if (elFuelVal) elFuelVal.innerText = `${Math.round(player.fuel)}%`;

      const elHeatBar = document.getElementById("sim-heat-bar");
      const elHeatVal = document.getElementById("sim-heat-val");
      if (elHeatBar) {
        elHeatBar.style.width = `${player.heat}%`;
        if (player.heat > 85) {
          elHeatBar.className = "h-full transition-all duration-75 bg-red-500";
        } else if (player.heat > 50) {
          elHeatBar.className = "h-full transition-all duration-75 bg-amber-500";
        } else {
          elHeatBar.className = "h-full transition-all duration-75 bg-cyan-400";
        }
      }
      if (elHeatVal) {
        if (player.heat > 85) {
          elHeatVal.innerText = "OVERHEATING";
          elHeatVal.className = "text-red-400 animate-pulse";
        } else {
          elHeatVal.innerText = `${Math.round(player.heat)}%`;
          elHeatVal.className = "text-white";
        }
      }

      const elShieldBar = document.getElementById("sim-shield-bar");
      const elShieldVal = document.getElementById("sim-shield-val");
      if (elShieldBar) elShieldBar.style.width = `${player.shield}%`;
      if (elShieldVal) elShieldVal.innerText = `${player.shield}%`;

      // Death condition: fell off the screen
      if (player.y + cameraY > viewH + 100) {
        setGameOver(true);
        setScore(currentScore);
        setActiveZone(zone);
        if (currentScore > maxScore) {
          setMaxScore(currentScore);
          try {
            localStorage.setItem("sim-max-score", currentScore.toString());
          } catch (e) {
            console.warn("Could not save score", e);
          }
        }
        if (soundRef.current) soundRef.current.playExplode();
      }

      // --- 2. Rendering ---

      // Clear Canvas
      ctx.clearRect(0, 0, viewW, viewH);

      // Dynamic backgrounds matching the active biome
      let bgGrad = ctx.createLinearGradient(0, 0, 0, viewH);
      if (zone === "Earth") {
        bgGrad.addColorStop(0, "#1A237E");
        bgGrad.addColorStop(0.5, "#2196F3");
        bgGrad.addColorStop(1, "#1A0033");
      } else if (zone === "Cloud Layer") {
        bgGrad.addColorStop(0, "#00BCD4");
        bgGrad.addColorStop(0.5, "#311B92");
        bgGrad.addColorStop(1, "#0D001A");
      } else {
        bgGrad.addColorStop(0, "#000000");
        bgGrad.addColorStop(0.5, "#0D001A");
        bgGrad.addColorStop(1, "#050308");
      }
      ctx.fillStyle = bgGrad;
      ctx.fillRect(0, 0, viewW, viewH);

      // Draw background decorations (stars / cloud outlines)
      ctx.save();
      ctx.translate(0, cameraY * 0.15); // Parallax factor
      if (zone === "Orbit" || zone === "Cloud Layer") {
        ctx.fillStyle = "rgba(255, 255, 255, 0.4)";
        // Generate pseudo-stars based on index
        for (let j = 0; j < 30; j++) {
          const sx = (j * 179) % viewW;
          const sy = (j * 231) % (viewH * 3) - 500;
          ctx.beginPath();
          ctx.arc(sx, sy, 0.8 + (j % 2) * 0.5, 0, Math.PI * 2);
          ctx.fill();
        }
      }
      ctx.restore();

      // Translate Canvas for camera follow
      ctx.save();
      ctx.translate(0, cameraY);

      // Draw Platforms
      platforms.forEach((p) => {
        if (p.y === 9999) return;
        
        let pColor = "rgba(255, 255, 255, 0.7)";
        let shadowColor = "rgba(255, 255, 255, 0.2)";
        if (p.type === "MOVING") { pColor = "#00E5FF"; shadowColor = "rgba(0, 229, 255, 0.4)"; }
        else if (p.type === "BOOST") { pColor = "#FFD700"; shadowColor = "rgba(255, 215, 0, 0.5)"; }
        else if (p.type === "ICE") { pColor = "#80DEEA"; shadowColor = "rgba(128, 222, 234, 0.3)"; }
        else if (p.type === "BREAKABLE") {
          pColor = p.shatterTimer !== undefined ? "rgba(255, 23, 68, 0.4)" : "#FF1744";
          shadowColor = "rgba(255, 23, 68, 0.3)";
        }

        ctx.fillStyle = pColor;
        ctx.shadowColor = shadowColor;
        ctx.shadowBlur = 8;
        
        // Draw platform body
        ctx.beginPath();
        if (typeof (ctx as any).roundRect === "function") {
          (ctx as any).roundRect(p.x, p.y, p.width, p.height, 3);
        } else {
          ctx.rect(p.x, p.y, p.width, p.height);
        }
        ctx.fill();
        ctx.shadowBlur = 0; // reset shadow

        // Top line highlight
        ctx.strokeStyle = "rgba(255, 255, 255, 0.4)";
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(p.x + 2, p.y);
        ctx.lineTo(p.x + p.width - 2, p.y);
        ctx.stroke();

        // Platform specific icons / designs
        if (p.type === "BOOST") {
          ctx.fillStyle = "#FFFFFF";
          ctx.beginPath();
          ctx.moveTo(p.x + p.width / 2, p.y + 2);
          ctx.lineTo(p.x + p.width / 2 - 4, p.y + 7);
          ctx.lineTo(p.x + p.width / 2 + 4, p.y + 7);
          ctx.fill();
        }
      });

      // Draw Particles
      particles.forEach((part) => {
        ctx.fillStyle = part.color;
        ctx.globalAlpha = part.life / part.maxLife;
        ctx.beginPath();
        ctx.arc(part.x, part.y, part.size, 0, Math.PI * 2);
        ctx.fill();
      });
      ctx.globalAlpha = 1.0;

      // Draw Droid / Rocket
      ctx.save();
      ctx.translate(player.x, player.y);
      
      // Rocket tilt based on horizontal velocity
      const tilt = Math.max(-0.25, Math.min(0.25, player.vx * 0.0008));
      ctx.rotate(tilt);

      // Rocket body (Vector lines)
      ctx.shadowColor = isThrusting ? "#00E5FF" : "rgba(255,255,255,0.2)";
      ctx.shadowBlur = 10;
      ctx.fillStyle = "#FFFFFF";
      
      // Diamond capsule body
      ctx.beginPath();
      ctx.moveTo(0, -18);
      ctx.lineTo(8, -6);
      ctx.lineTo(6, 12);
      ctx.lineTo(-6, 12);
      ctx.lineTo(-8, -6);
      ctx.closePath();
      ctx.fill();

      // Wing fins
      ctx.fillStyle = "#FF1744";
      ctx.beginPath();
      ctx.moveTo(-6, 4);
      ctx.lineTo(-12, 12);
      ctx.lineTo(-6, 12);
      ctx.closePath();
      ctx.fill();

      ctx.beginPath();
      ctx.moveTo(6, 4);
      ctx.lineTo(12, 12);
      ctx.lineTo(6, 12);
      ctx.closePath();
      ctx.fill();

      // Glass Cockpit dome
      ctx.fillStyle = player.overheated ? "#FF1744" : "#00E5FF";
      ctx.beginPath();
      ctx.arc(0, -3, 3.5, 0, Math.PI * 2);
      ctx.fill();
      ctx.shadowBlur = 0;

      // Engine nozzle
      ctx.fillStyle = "#37474F";
      ctx.fillRect(-3, 12, 6, 3);

      ctx.restore();

      ctx.restore(); // Restore camera translation

      if (isPlaying && !gameOver) {
        animationId = requestAnimationFrame(gameLoop);
      }
    };

    animationId = requestAnimationFrame(gameLoop);
    return () => {
      cancelAnimationFrame(animationId);
    };
  }, [isPlaying, gameOver, maxScore]);

  // Handle canvas mouse and touch interactions
  const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const clientX = e.clientX - rect.left;
    mouseTargetX.current = clientX * (canvas.width / rect.width);
  };

  const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>) => {
    isThrustingMouse.current = true;
    const canvas = canvasRef.current;
    if (canvas) {
      const rect = canvas.getBoundingClientRect();
      const clientX = e.clientX - rect.left;
      mouseTargetX.current = clientX * (canvas.width / rect.width);
    }
    if (soundRef.current) soundRef.current.playThrust(true);
  };

  const handleMouseUp = () => {
    isThrustingMouse.current = false;
  };

  const handleTouchStart = (e: React.TouchEvent<HTMLCanvasElement>) => {
    if (e.cancelable) e.preventDefault();
    isThrustingMouse.current = true;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const touchX = e.touches[0].clientX - rect.left;
    mouseTargetX.current = touchX * (canvas.width / rect.width);
    if (soundRef.current) soundRef.current.playThrust(true);
  };

  const handleTouchMove = (e: React.TouchEvent<HTMLCanvasElement>) => {
    if (e.cancelable) e.preventDefault();
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const touchX = e.touches[0].clientX - rect.left;
    mouseTargetX.current = touchX * (canvas.width / rect.width);
  };

  const handleTouchEnd = () => {
    isThrustingMouse.current = false;
    mouseTargetX.current = null;
  };

  return (
    <section id="simulation" className="relative py-24 border-t border-white/10 bg-black/90">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(0,229,255,0.06),transparent_35%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <div className="mb-12 max-w-2xl space-y-4">
          <p className="text-sm uppercase tracking-[0.35em] text-cyan-300 font-extrabold bg-cyan-400/10 px-3 py-1 rounded-full border border-cyan-400/20 inline-block">
            Simulation Bay
          </p>
          <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
            Hangar Launch Simulator
          </h2>
          <p className="text-slate-300 text-sm leading-relaxed">
            Test the vertical thruster physics controls directly in your browser. Click/Tap or hold the simulator to activate rocket boosters and steer by sliding. Keep the engine cool and stay on platforms!
          </p>
        </div>

        <div className="grid gap-12 lg:grid-cols-12 items-center">
          {/* Main simulator screen (7 Columns) */}
          <div ref={containerRef} className="lg:col-span-7 flex justify-center">
            <div className="relative rounded-3xl border-2 border-cyan-300/15 bg-slate-950 p-4 shadow-[0_0_50px_rgba(0,229,255,0.1)] overflow-hidden max-w-md w-full">
              {!isPlaying ? (
                <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-black/85 p-8 text-center">
                  <p className="text-xs uppercase tracking-[0.4em] text-cyan-400 font-bold mb-3">Expedition Training Mode</p>
                  <h3 className="text-2xl font-black text-white tracking-wider mb-2">SIMULATION OFFLINE</h3>
                  <p className="text-xs text-slate-400 max-w-xs mb-8">
                    Calibrate guidance sensors and engine heat logs. Click start to engage localized gravity systems.
                  </p>
                  <button
                    onClick={startGame}
                    className="rounded-full bg-cyan-400 px-8 py-3.5 text-xs font-bold uppercase tracking-[0.2em] text-slate-950 hover:bg-cyan-300 transition cursor-pointer shadow-[0_0_20px_rgba(0,229,255,0.4)]"
                  >
                    Engage Thrusters
                  </button>
                </div>
              ) : gameOver ? (
                <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-black/90 p-8 text-center">
                  <p className="text-xs uppercase tracking-[0.4em] text-red-500 font-bold mb-3">System Critical Failure</p>
                  <h3 className="text-2xl font-black text-white tracking-wider mb-2">DROID CRASHED</h3>
                  <p className="text-xs text-slate-400 mb-2">Altitude reached: <span className="text-cyan-300 font-bold">{score}m</span></p>
                  <p className="text-xs text-slate-400 mb-8">Hangar Record: <span className="text-yellow-400 font-bold">{maxScore}m</span></p>
                  <button
                    onClick={startGame}
                    className="rounded-full bg-red-500 px-8 py-3.5 text-xs font-bold uppercase tracking-[0.2em] text-white hover:bg-red-400 transition cursor-pointer shadow-[0_0_20px_rgba(239,68,68,0.4)]"
                  >
                    Relaunch Droid
                  </button>
                </div>
              ) : null}

              <canvas
                ref={canvasRef}
                onMouseMove={handleMouseMove}
                onMouseDown={handleMouseDown}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
                onTouchStart={handleTouchStart}
                onTouchMove={handleTouchMove}
                onTouchEnd={handleTouchEnd}
                className="block bg-black rounded-2xl w-full min-h-[300px] sm:h-[520px] cursor-crosshair touch-none"
                aria-label="Game simulator canvas. Press Space or hold mouse to thrust, move left or right to steer."
              />
            </div>
          </div>

          {/* Telemetry dashboard (5 Columns) */}
          <div className="lg:col-span-5 space-y-6">
            <div className="rounded-3xl border border-cyan-300/10 bg-slate-950/40 p-6 backdrop-blur-md space-y-6" aria-live="polite" aria-label="Simulator telemetry">
              <h3 className="text-xs uppercase tracking-[0.25em] text-cyan-300 font-black border-b border-white/5 pb-3">
                Telemetry Output
              </h3>

              {/* Altitude readout */}
              <div className="flex justify-between items-baseline">
                <span className="text-xs text-slate-400 font-mono">ALTITUDE:</span>
                <span id="sim-score" className="text-4xl font-black font-mono tracking-tight text-white">{score}m</span>
              </div>

              {/* Biome classification */}
              <div className="flex justify-between items-center">
                <span className="text-xs text-slate-400 font-mono">SECTOR:</span>
                <span id="sim-zone" className="text-xs font-bold uppercase tracking-wider text-cyan-300 bg-cyan-400/10 px-2.5 py-0.5 rounded border border-cyan-400/20">
                  {activeZone}
                </span>
              </div>

              {/* Fuel Gauges */}
              <div className="space-y-2">
                <div className="flex justify-between text-[10px] font-bold font-mono text-slate-400">
                  <span>FUEL INJECTION</span>
                  <span id="sim-fuel-val" className="text-white">{Math.round(fuel)}%</span>
                </div>
                <div className="h-2 w-full rounded-full bg-white/5 overflow-hidden">
                  <div
                    id="sim-fuel-bar"
                    className="h-full bg-emerald-500 transition-all duration-75"
                    style={{ width: `${fuel}%` }}
                  />
                </div>
              </div>

              {/* Heat Gauges */}
              <div className="space-y-2">
                <div className="flex justify-between text-[10px] font-bold font-mono text-slate-400">
                  <span>ENGINE CORE TEMPERATURE</span>
                  <span id="sim-heat-val" className={heat > 85 ? "text-red-400 animate-pulse" : "text-white"}>
                    {heat > 85 ? "OVERHEATING" : `${Math.round(heat)}%`}
                  </span>
                </div>
                <div className="h-2 w-full rounded-full bg-white/5 overflow-hidden">
                  <div
                    id="sim-heat-bar"
                    className={`h-full transition-all duration-75 ${
                      heat > 85 ? "bg-red-500" : heat > 50 ? "bg-amber-500" : "bg-cyan-400"
                    }`}
                    style={{ width: `${heat}%` }}
                  />
                </div>
              </div>

              {/* Shield Gauges */}
              <div className="space-y-2">
                <div className="flex justify-between text-[10px] font-bold font-mono text-slate-400">
                  <span>SHIELD INTEGRITY</span>
                  <span id="sim-shield-val" className="text-white">{shield}%</span>
                </div>
                <div className="h-2 w-full rounded-full bg-white/5 overflow-hidden">
                  <div
                    id="sim-shield-bar"
                    className="h-full bg-blue-500"
                    style={{ width: `${shield}%` }}
                  />
                </div>
              </div>
            </div>

            {/* Instruction logs */}
            <div className="rounded-3xl border border-white/5 bg-slate-950/20 p-6 space-y-4">
              <p className="text-[10px] font-black uppercase tracking-widest text-slate-400">Controls Log</p>
              <ul className="text-xs text-slate-300 space-y-2.5 font-mono">
                <li><span className="text-cyan-300 font-bold">Spacebar / Mouse Hold</span>: Fire Thrusters</li>
                <li><span className="text-cyan-300 font-bold">Left/Right Arrow (or Mouse Move)</span>: Steer Droid</li>
                <li><span className="text-cyan-300 font-bold">Platform landing</span>: Instantly refuels thruster cells</li>
                <li><span className="text-yellow-400 font-bold">Gold Platforms</span>: Hyper boost velocity</li>
                <li><span className="text-red-400 font-bold">Red Platforms</span>: Crumble shortly after contact</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
