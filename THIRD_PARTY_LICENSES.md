# Third-Party Licenses

This project incorporates code adapted from third-party open source software.
The components below are used under their original license terms, reproduced
in full.

---

## Scannable

- **Source:** https://github.com/MightyPirates/Scannable
- **CurseForge:** https://www.curseforge.com/minecraft/mc-mods/scannable
- **Version referenced:** 1.21.11 branch
- **License:** MIT

Portions of the Divine Sense rendering implementation are adapted from, or
were developed with reference to, Scannable:

- The fragment shader's world-position reconstruction (_converting a sampled
  depth-buffer value and screen coordinates back into world space via the
  inverse view and projection matrices_) remains substantially derived from
  Scannable's `scan_effect.fsh`.
- The render-pass strategy (rendering into Minecraft's main color target
  while sampling the completed world depth texture, with no depth attachment
  on the effect pass) was developed from studying Scannable's
  `ScannerRenderer`. The surrounding renderer infrastructure has since been
  substantially rewritten around Ascension's own architecture.

The fullscreen geometry and vertex shader, propagation equation, pulse timing
and stat scaling, visual pulse mathematics, resonance and flare styling, and
entity highlighting are Ascension-specific and are no longer fully derived from
Scannable.

Full license text:

```
MIT License

Copyright (c) 2017-2026 Florian "Sangar" Nücke

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

---