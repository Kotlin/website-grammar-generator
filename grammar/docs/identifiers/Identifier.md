Depending on the target and publicity of the declaration, the set of allowed symbols in identifiers is different.
This rule contains the union of allowed symbols from all targets.
Thus, the code for any target can be parsed using grammar.

The allowed symbols in identifiers corresponding to the target and publicity of the declaration are given below.

#### Kotlin/JVM (for any publicity of declarations)

<div class="grammar-description">
    <span class="grammar-string">~</span>
    <span class="grammar-symbol">(</span>
    <span class="grammar-string">[\r\n]</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'`'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'.'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">';'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">':'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'\'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'/'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'['</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">']'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'<'</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'>'</span>
    <span class="grammar-symbol">)</span>
</div>

#### Kotlin/JS (for private declarations)

<div class="grammar-description">
    <span class="grammar-string">~</span>
    <span class="grammar-symbol">(</span>
    <span class="grammar-string">[\r\n]</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'`'</span>
    <span class="grammar-symbol">)</span>
</div>

#### Kotlin/JS (for public declarations)

The allowed symbols for public declarations correspond to the <a href="https://www.ecma-international.org/ecma-262/5.1/#sec-7.6" target="_blank">ECMA specification (section 7.6)</a> except that ECMA reserved words is allowed.

#### Kotlin/Native (for any publicity of declarations)

<div class="grammar-description">
    <span class="grammar-string">~</span>
    <span class="grammar-symbol">(</span>
    <span class="grammar-string">[\r\n]</span>
    <span class="grammar-symbol">|</span>
    <span class="grammar-string">'`'</span>
    <span class="grammar-symbol">)</span>
</div>