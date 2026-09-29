#!/usr/bin/env python3
import math
import os
import struct
import wave

AUDIO_DIR = '/root/SystemDesignLab/app/src/main/assets/course/audio'
os.makedirs(AUDIO_DIR, exist_ok=True)

def generate_tone_sequence(filename, duration_sec=30, sample_rate=22050):
    filepath = os.path.join(AUDIO_DIR, filename)
    num_samples = int(duration_sec * sample_rate)
    
    with wave.open(filepath, 'w') as wav:
        wav.setnchannels(1)  # Mono
        wav.setsampwidth(2)  # 16-bit
        wav.setframerate(sample_rate)
        
        # We will synthesize a warm ambient educational chime chord progression
        chords = [
            [261.63, 329.63, 392.00], # C Major
            [220.00, 261.63, 329.63], # A Minor
            [174.61, 220.00, 261.63], # F Major
            [196.00, 246.94, 293.66], # G Major
        ]
        
        samples = []
        for i in range(num_samples):
            t = i / sample_rate
            chord_idx = int((t / 4.0) % len(chords))
            chord = chords[chord_idx]
            
            # Sum sine waves with gentle envelope
            val = 0.0
            for freq in chord:
                val += math.sin(2.0 * math.pi * freq * t)
            
            # Ambient modulation
            val *= 0.25 * (0.8 + 0.2 * math.sin(2.0 * math.pi * 0.25 * t))
            
            # Subtle narration marker clicks every 5 seconds
            if (i % (sample_rate * 5)) < 400:
                val += 0.3 * math.sin(2.0 * math.pi * 880.0 * t)
                
            sample_val = int(max(-32767, min(32767, val * 20000)))
            samples.append(struct.pack('<h', sample_val))
            
        wav.writeframes(b''.join(samples))
        print(f"Generated {filepath} ({duration_sec}s, {os.path.getsize(filepath)} bytes)")

def main():
    for ch in range(1, 6):
        generate_tone_sequence(f"chapter_0{ch}.wav", duration_sec=30)

if __name__ == '__main__':
    main()
