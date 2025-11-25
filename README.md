# PGRF1 - Úloha 3: 3D Drátový Renderer

Semestrální projekt z předmětu **Počítačová grafika I (PGRF1)** na FIM UHK.
Tato aplikace je kompletní implementací 3D grafického pipeline od nuly (bez použití externích grafických knihoven jako OpenGL/DirectX) v jazyce Java.

---

##  Implementované funkce

Projekt splňuje všechny povinné požadavky i bonusové úkoly:

### 1. Jádro aplikace (Vlastní matematická knihovna)
Aplikace nevyužívá žádné externí matematické knihovny. Veškerá logika je implementována v balíčku `transforms`:
* **Matice (`Mat4`):** Implementace matic 4x4 a operací s nimi (násobení).
* **Vektory (`Vec3D`, `Point3D`):** Práce s homogenními souřadnicemi.
* **Transformace:** Implementace matic pro posun (`Mat4Transl`), rotaci (`Mat4Rot`) a změnu měřítka (`Mat4Scale`).

### 2. 3D Pipeline (Renderer)
Vlastní implementace vykreslovacího řetězce v třídě `Renderer3D`:
* **Transformace:** Skládání matic v pořadí `Projection * View * Model`.
* **Projekce:** Perspektivní projekce (`Mat4PerspRH`).
* **Ořezání (Clipping):** Implementováno ořezání těles, která jsou za kamerou (Near-plane clipping).
* **Dehomogenizace:** Perspektivní dělení (převod z homogenních souřadnic).
* **Viewport:** Transformace do souřadnic okna.

### 3. Obsah scény (Tělesa)
Scéna obsahuje zástupce všech povinných skupin těles (balíček `solid`):
* **Skupina 1:** **Krychle (Cube)** - fialová.
* **Skupina 2:** **Jehlan (Pyramid)** - žlutý.
* **Skupina 3:** **Bézierova křivka** - azurový oblouk definovaný 4 řídícími body.
* **Osy:** Zobrazení 3D os (červená=X, zelená=Y, modrá=Z) pro lepší orientaci.

###  Bonusové funkce
* **Bonus 1:** **Parametrická plocha (Surface)** - generovaná mřížka definovaná matematickou funkcí (vlnobití), zobrazená azurovou barvou pod scénou.
* **Bonus 2:** **Animace** - parametrická plocha se plynule otáčí kolem svislé osy pomocí časovače (`Timer`).

---

##  Ovládání

Aplikace se ovládá interaktivně pomocí klávesnice a myši:

| Klávesa / Akce | Funkce |
| :--- | :--- |
| **W, S** | Pohyb kamery dopředu / dozadu |
| **A, D** | Pohyb kamery vlevo / vpravo (Strafe) |
| **Myš (táhnout)** | Rozhlížení (změna úhlu pohledu) |
| **R** | **Reset** kamery do výchozí izometrické pozice |

---

##  Struktura projektu

Kód je rozdělen do logických balíčků:

* **`transforms`**: Matematické jádro (matice, vektory, kamera).
* **`solid`**: Definice 3D modelů (Vertex Buffer, Index Buffer) a křivek.
* **`renderer3d`**: Logika vykreslování a transformací.
* **`controller`**: Řízení aplikace, inicializace scény a obsluha vstupů.
* **`rasterize`**: Nízkoúrovňové kreslení úseček (LineRasterizer).
* **`view`**: Okenní komponenty (Swing).

---
*Vypracoval: Maksym Makedonskyi*