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

Portions of the following files in this project are adapted from Scannable:

- The fullscreen vertex shader — near line-for-line reuse of Scannable's
  fullscreen-triangle-from-`gl_VertexID` implementation.
- The fragment shader — world-position reconstruction and expanding
  spherical-shell algorithm, adapted with different colors, shell width,
  scanline intensity, depth-mode support, and naming.
- `DivineSenseRenderer` — adapted from `ScannerRenderer`; the core render
  sequence (main render target → depth texture → inverted view/projection →
  packed UBO → color pass with no depth attachment → bind depth sampler →
  fullscreen `draw(0, 3)`) is directly based on Scannable's implementation.
- `DivineSensePipelines` — adapted from Scannable's pipeline configuration
  (`POST_PROCESSING_SNIPPET`, empty vertex format, triangle mode, depth
  sampler, UBO, additive blend, disabled culling), with Ascension's 26.1
  pipeline registration and API differences layered on top.
- The pulse/radius progression equation — the quadratic growth behavior is
  based on Scannable's radius progression, simplified around Ascension's
  fixed skill radius and duration.

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