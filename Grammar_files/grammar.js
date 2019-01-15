webpackJsonp([5],{

/***/ 180:
/***/ (function(module, exports, __webpack_require__) {

exports.f = __webpack_require__(61);


/***/ }),

/***/ 181:
/***/ (function(module, exports, __webpack_require__) {

var global = __webpack_require__(82);
var core = __webpack_require__(83);
var LIBRARY = __webpack_require__(325);
var wksExt = __webpack_require__(180);
var defineProperty = __webpack_require__(84).f;
module.exports = function (name) {
  var $Symbol = core.Symbol || (core.Symbol = LIBRARY ? {} : global.Symbol || {});
  if (name.charAt(0) != '_' && !(name in $Symbol)) defineProperty($Symbol, name, { value: wksExt.f(name) });
};


/***/ }),

/***/ 182:
/***/ (function(module, exports) {

exports.f = {}.propertyIsEnumerable;


/***/ }),

/***/ 327:
/***/ (function(module, exports) {

exports.f = Object.getOwnPropertySymbols;


/***/ }),

/***/ 328:
/***/ (function(module, exports, __webpack_require__) {

// 19.1.2.7 / 15.2.3.4 Object.getOwnPropertyNames(O)
var $keys = __webpack_require__(707);
var hiddenKeys = __webpack_require__(336).concat('length', 'prototype');

exports.f = Object.getOwnPropertyNames || function getOwnPropertyNames(O) {
  return $keys(O, hiddenKeys);
};


/***/ }),

/***/ 697:
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.__esModule = true;

var _iterator = __webpack_require__(698);

var _iterator2 = _interopRequireDefault(_iterator);

var _symbol = __webpack_require__(714);

var _symbol2 = _interopRequireDefault(_symbol);

var _typeof = typeof _symbol2.default === "function" && typeof _iterator2.default === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof _symbol2.default === "function" && obj.constructor === _symbol2.default && obj !== _symbol2.default.prototype ? "symbol" : typeof obj; };

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = typeof _symbol2.default === "function" && _typeof(_iterator2.default) === "symbol" ? function (obj) {
  return typeof obj === "undefined" ? "undefined" : _typeof(obj);
} : function (obj) {
  return obj && typeof _symbol2.default === "function" && obj.constructor === _symbol2.default && obj !== _symbol2.default.prototype ? "symbol" : typeof obj === "undefined" ? "undefined" : _typeof(obj);
};

/***/ }),

/***/ 698:
/***/ (function(module, exports, __webpack_require__) {

module.exports = { "default": __webpack_require__(699), __esModule: true };

/***/ }),

/***/ 699:
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(700);
__webpack_require__(710);
module.exports = __webpack_require__(180).f('iterator');


/***/ }),

/***/ 710:
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(711);
var global = __webpack_require__(82);
var hide = __webpack_require__(120);
var Iterators = __webpack_require__(179);
var TO_STRING_TAG = __webpack_require__(61)('toStringTag');

var DOMIterables = ('CSSRuleList,CSSStyleDeclaration,CSSValueList,ClientRectList,DOMRectList,DOMStringList,' +
  'DOMTokenList,DataTransferItemList,FileList,HTMLAllCollection,HTMLCollection,HTMLFormElement,HTMLSelectElement,' +
  'MediaList,MimeTypeArray,NamedNodeMap,NodeList,PaintRequestList,Plugin,PluginArray,SVGLengthList,SVGNumberList,' +
  'SVGPathSegList,SVGPointList,SVGStringList,SVGTransformList,SourceBufferList,StyleSheetList,TextTrackCueList,' +
  'TextTrackList,TouchList').split(',');

for (var i = 0; i < DOMIterables.length; i++) {
  var NAME = DOMIterables[i];
  var Collection = global[NAME];
  var proto = Collection && Collection.prototype;
  if (proto && !proto[TO_STRING_TAG]) hide(proto, TO_STRING_TAG, NAME);
  Iterators[NAME] = Iterators.Array;
}


/***/ }),

/***/ 711:
/***/ (function(module, exports, __webpack_require__) {

"use strict";

var addToUnscopables = __webpack_require__(712);
var step = __webpack_require__(713);
var Iterators = __webpack_require__(179);
var toIObject = __webpack_require__(86);

// 22.1.3.4 Array.prototype.entries()
// 22.1.3.13 Array.prototype.keys()
// 22.1.3.29 Array.prototype.values()
// 22.1.3.30 Array.prototype[@@iterator]()
module.exports = __webpack_require__(703)(Array, 'Array', function (iterated, kind) {
  this._t = toIObject(iterated); // target
  this._i = 0;                   // next index
  this._k = kind;                // kind
// 22.1.5.2.1 %ArrayIteratorPrototype%.next()
}, function () {
  var O = this._t;
  var kind = this._k;
  var index = this._i++;
  if (!O || index >= O.length) {
    this._t = undefined;
    return step(1);
  }
  if (kind == 'keys') return step(0, index);
  if (kind == 'values') return step(0, O[index]);
  return step(0, [index, O[index]]);
}, 'values');

// argumentsList[@@iterator] is %ArrayProto_values% (9.4.4.6, 9.4.4.7)
Iterators.Arguments = Iterators.Array;

addToUnscopables('keys');
addToUnscopables('values');
addToUnscopables('entries');


/***/ }),

/***/ 712:
/***/ (function(module, exports) {

module.exports = function () { /* empty */ };


/***/ }),

/***/ 713:
/***/ (function(module, exports) {

module.exports = function (done, value) {
  return { value: value, done: !!done };
};


/***/ }),

/***/ 714:
/***/ (function(module, exports, __webpack_require__) {

module.exports = { "default": __webpack_require__(715), __esModule: true };

/***/ }),

/***/ 715:
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(716);
__webpack_require__(722);
__webpack_require__(723);
__webpack_require__(724);
module.exports = __webpack_require__(83).Symbol;


/***/ }),

/***/ 716:
/***/ (function(module, exports, __webpack_require__) {

"use strict";

// ECMAScript 6 symbols shim
var global = __webpack_require__(82);
var has = __webpack_require__(85);
var DESCRIPTORS = __webpack_require__(118);
var $export = __webpack_require__(187);
var redefine = __webpack_require__(705);
var META = __webpack_require__(717).KEY;
var $fails = __webpack_require__(177);
var shared = __webpack_require__(335);
var setToStringTag = __webpack_require__(337);
var uid = __webpack_require__(190);
var wks = __webpack_require__(61);
var wksExt = __webpack_require__(180);
var wksDefine = __webpack_require__(181);
var enumKeys = __webpack_require__(718);
var isArray = __webpack_require__(719);
var anObject = __webpack_require__(188);
var isObject = __webpack_require__(176);
var toIObject = __webpack_require__(86);
var toPrimitive = __webpack_require__(326);
var createDesc = __webpack_require__(178);
var _create = __webpack_require__(706);
var gOPNExt = __webpack_require__(720);
var $GOPD = __webpack_require__(721);
var $DP = __webpack_require__(84);
var $keys = __webpack_require__(189);
var gOPD = $GOPD.f;
var dP = $DP.f;
var gOPN = gOPNExt.f;
var $Symbol = global.Symbol;
var $JSON = global.JSON;
var _stringify = $JSON && $JSON.stringify;
var PROTOTYPE = 'prototype';
var HIDDEN = wks('_hidden');
var TO_PRIMITIVE = wks('toPrimitive');
var isEnum = {}.propertyIsEnumerable;
var SymbolRegistry = shared('symbol-registry');
var AllSymbols = shared('symbols');
var OPSymbols = shared('op-symbols');
var ObjectProto = Object[PROTOTYPE];
var USE_NATIVE = typeof $Symbol == 'function';
var QObject = global.QObject;
// Don't use setters in Qt Script, https://github.com/zloirock/core-js/issues/173
var setter = !QObject || !QObject[PROTOTYPE] || !QObject[PROTOTYPE].findChild;

// fallback for old Android, https://code.google.com/p/v8/issues/detail?id=687
var setSymbolDesc = DESCRIPTORS && $fails(function () {
  return _create(dP({}, 'a', {
    get: function () { return dP(this, 'a', { value: 7 }).a; }
  })).a != 7;
}) ? function (it, key, D) {
  var protoDesc = gOPD(ObjectProto, key);
  if (protoDesc) delete ObjectProto[key];
  dP(it, key, D);
  if (protoDesc && it !== ObjectProto) dP(ObjectProto, key, protoDesc);
} : dP;

var wrap = function (tag) {
  var sym = AllSymbols[tag] = _create($Symbol[PROTOTYPE]);
  sym._k = tag;
  return sym;
};

var isSymbol = USE_NATIVE && typeof $Symbol.iterator == 'symbol' ? function (it) {
  return typeof it == 'symbol';
} : function (it) {
  return it instanceof $Symbol;
};

var $defineProperty = function defineProperty(it, key, D) {
  if (it === ObjectProto) $defineProperty(OPSymbols, key, D);
  anObject(it);
  key = toPrimitive(key, true);
  anObject(D);
  if (has(AllSymbols, key)) {
    if (!D.enumerable) {
      if (!has(it, HIDDEN)) dP(it, HIDDEN, createDesc(1, {}));
      it[HIDDEN][key] = true;
    } else {
      if (has(it, HIDDEN) && it[HIDDEN][key]) it[HIDDEN][key] = false;
      D = _create(D, { enumerable: createDesc(0, false) });
    } return setSymbolDesc(it, key, D);
  } return dP(it, key, D);
};
var $defineProperties = function defineProperties(it, P) {
  anObject(it);
  var keys = enumKeys(P = toIObject(P));
  var i = 0;
  var l = keys.length;
  var key;
  while (l > i) $defineProperty(it, key = keys[i++], P[key]);
  return it;
};
var $create = function create(it, P) {
  return P === undefined ? _create(it) : $defineProperties(_create(it), P);
};
var $propertyIsEnumerable = function propertyIsEnumerable(key) {
  var E = isEnum.call(this, key = toPrimitive(key, true));
  if (this === ObjectProto && has(AllSymbols, key) && !has(OPSymbols, key)) return false;
  return E || !has(this, key) || !has(AllSymbols, key) || has(this, HIDDEN) && this[HIDDEN][key] ? E : true;
};
var $getOwnPropertyDescriptor = function getOwnPropertyDescriptor(it, key) {
  it = toIObject(it);
  key = toPrimitive(key, true);
  if (it === ObjectProto && has(AllSymbols, key) && !has(OPSymbols, key)) return;
  var D = gOPD(it, key);
  if (D && has(AllSymbols, key) && !(has(it, HIDDEN) && it[HIDDEN][key])) D.enumerable = true;
  return D;
};
var $getOwnPropertyNames = function getOwnPropertyNames(it) {
  var names = gOPN(toIObject(it));
  var result = [];
  var i = 0;
  var key;
  while (names.length > i) {
    if (!has(AllSymbols, key = names[i++]) && key != HIDDEN && key != META) result.push(key);
  } return result;
};
var $getOwnPropertySymbols = function getOwnPropertySymbols(it) {
  var IS_OP = it === ObjectProto;
  var names = gOPN(IS_OP ? OPSymbols : toIObject(it));
  var result = [];
  var i = 0;
  var key;
  while (names.length > i) {
    if (has(AllSymbols, key = names[i++]) && (IS_OP ? has(ObjectProto, key) : true)) result.push(AllSymbols[key]);
  } return result;
};

// 19.4.1.1 Symbol([description])
if (!USE_NATIVE) {
  $Symbol = function Symbol() {
    if (this instanceof $Symbol) throw TypeError('Symbol is not a constructor!');
    var tag = uid(arguments.length > 0 ? arguments[0] : undefined);
    var $set = function (value) {
      if (this === ObjectProto) $set.call(OPSymbols, value);
      if (has(this, HIDDEN) && has(this[HIDDEN], tag)) this[HIDDEN][tag] = false;
      setSymbolDesc(this, tag, createDesc(1, value));
    };
    if (DESCRIPTORS && setter) setSymbolDesc(ObjectProto, tag, { configurable: true, set: $set });
    return wrap(tag);
  };
  redefine($Symbol[PROTOTYPE], 'toString', function toString() {
    return this._k;
  });

  $GOPD.f = $getOwnPropertyDescriptor;
  $DP.f = $defineProperty;
  __webpack_require__(328).f = gOPNExt.f = $getOwnPropertyNames;
  __webpack_require__(182).f = $propertyIsEnumerable;
  __webpack_require__(327).f = $getOwnPropertySymbols;

  if (DESCRIPTORS && !__webpack_require__(325)) {
    redefine(ObjectProto, 'propertyIsEnumerable', $propertyIsEnumerable, true);
  }

  wksExt.f = function (name) {
    return wrap(wks(name));
  };
}

$export($export.G + $export.W + $export.F * !USE_NATIVE, { Symbol: $Symbol });

for (var es6Symbols = (
  // 19.4.2.2, 19.4.2.3, 19.4.2.4, 19.4.2.6, 19.4.2.8, 19.4.2.9, 19.4.2.10, 19.4.2.11, 19.4.2.12, 19.4.2.13, 19.4.2.14
  'hasInstance,isConcatSpreadable,iterator,match,replace,search,species,split,toPrimitive,toStringTag,unscopables'
).split(','), j = 0; es6Symbols.length > j;)wks(es6Symbols[j++]);

for (var wellKnownSymbols = $keys(wks.store), k = 0; wellKnownSymbols.length > k;) wksDefine(wellKnownSymbols[k++]);

$export($export.S + $export.F * !USE_NATIVE, 'Symbol', {
  // 19.4.2.1 Symbol.for(key)
  'for': function (key) {
    return has(SymbolRegistry, key += '')
      ? SymbolRegistry[key]
      : SymbolRegistry[key] = $Symbol(key);
  },
  // 19.4.2.5 Symbol.keyFor(sym)
  keyFor: function keyFor(sym) {
    if (!isSymbol(sym)) throw TypeError(sym + ' is not a symbol!');
    for (var key in SymbolRegistry) if (SymbolRegistry[key] === sym) return key;
  },
  useSetter: function () { setter = true; },
  useSimple: function () { setter = false; }
});

$export($export.S + $export.F * !USE_NATIVE, 'Object', {
  // 19.1.2.2 Object.create(O [, Properties])
  create: $create,
  // 19.1.2.4 Object.defineProperty(O, P, Attributes)
  defineProperty: $defineProperty,
  // 19.1.2.3 Object.defineProperties(O, Properties)
  defineProperties: $defineProperties,
  // 19.1.2.6 Object.getOwnPropertyDescriptor(O, P)
  getOwnPropertyDescriptor: $getOwnPropertyDescriptor,
  // 19.1.2.7 Object.getOwnPropertyNames(O)
  getOwnPropertyNames: $getOwnPropertyNames,
  // 19.1.2.8 Object.getOwnPropertySymbols(O)
  getOwnPropertySymbols: $getOwnPropertySymbols
});

// 24.3.2 JSON.stringify(value [, replacer [, space]])
$JSON && $export($export.S + $export.F * (!USE_NATIVE || $fails(function () {
  var S = $Symbol();
  // MS Edge converts symbol values to JSON as {}
  // WebKit converts symbol values to JSON as null
  // V8 throws on boxed symbols
  return _stringify([S]) != '[null]' || _stringify({ a: S }) != '{}' || _stringify(Object(S)) != '{}';
})), 'JSON', {
  stringify: function stringify(it) {
    var args = [it];
    var i = 1;
    var replacer, $replacer;
    while (arguments.length > i) args.push(arguments[i++]);
    $replacer = replacer = args[1];
    if (!isObject(replacer) && it === undefined || isSymbol(it)) return; // IE8 returns string on undefined
    if (!isArray(replacer)) replacer = function (key, value) {
      if (typeof $replacer == 'function') value = $replacer.call(this, key, value);
      if (!isSymbol(value)) return value;
    };
    args[1] = replacer;
    return _stringify.apply($JSON, args);
  }
});

// 19.4.3.4 Symbol.prototype[@@toPrimitive](hint)
$Symbol[PROTOTYPE][TO_PRIMITIVE] || __webpack_require__(120)($Symbol[PROTOTYPE], TO_PRIMITIVE, $Symbol[PROTOTYPE].valueOf);
// 19.4.3.5 Symbol.prototype[@@toStringTag]
setToStringTag($Symbol, 'Symbol');
// 20.2.1.9 Math[@@toStringTag]
setToStringTag(Math, 'Math', true);
// 24.3.3 JSON[@@toStringTag]
setToStringTag(global.JSON, 'JSON', true);


/***/ }),

/***/ 717:
/***/ (function(module, exports, __webpack_require__) {

var META = __webpack_require__(190)('meta');
var isObject = __webpack_require__(176);
var has = __webpack_require__(85);
var setDesc = __webpack_require__(84).f;
var id = 0;
var isExtensible = Object.isExtensible || function () {
  return true;
};
var FREEZE = !__webpack_require__(177)(function () {
  return isExtensible(Object.preventExtensions({}));
});
var setMeta = function (it) {
  setDesc(it, META, { value: {
    i: 'O' + ++id, // object ID
    w: {}          // weak collections IDs
  } });
};
var fastKey = function (it, create) {
  // return primitive with prefix
  if (!isObject(it)) return typeof it == 'symbol' ? it : (typeof it == 'string' ? 'S' : 'P') + it;
  if (!has(it, META)) {
    // can't set metadata to uncaught frozen object
    if (!isExtensible(it)) return 'F';
    // not necessary to add metadata
    if (!create) return 'E';
    // add missing metadata
    setMeta(it);
  // return object ID
  } return it[META].i;
};
var getWeak = function (it, create) {
  if (!has(it, META)) {
    // can't set metadata to uncaught frozen object
    if (!isExtensible(it)) return true;
    // not necessary to add metadata
    if (!create) return false;
    // add missing metadata
    setMeta(it);
  // return hash weak collections IDs
  } return it[META].w;
};
// add metadata on freeze-family methods calling
var onFreeze = function (it) {
  if (FREEZE && meta.NEED && isExtensible(it) && !has(it, META)) setMeta(it);
  return it;
};
var meta = module.exports = {
  KEY: META,
  NEED: false,
  fastKey: fastKey,
  getWeak: getWeak,
  onFreeze: onFreeze
};


/***/ }),

/***/ 718:
/***/ (function(module, exports, __webpack_require__) {

// all enumerable object keys, includes symbols
var getKeys = __webpack_require__(189);
var gOPS = __webpack_require__(327);
var pIE = __webpack_require__(182);
module.exports = function (it) {
  var result = getKeys(it);
  var getSymbols = gOPS.f;
  if (getSymbols) {
    var symbols = getSymbols(it);
    var isEnum = pIE.f;
    var i = 0;
    var key;
    while (symbols.length > i) if (isEnum.call(it, key = symbols[i++])) result.push(key);
  } return result;
};


/***/ }),

/***/ 719:
/***/ (function(module, exports, __webpack_require__) {

// 7.2.2 IsArray(argument)
var cof = __webpack_require__(334);
module.exports = Array.isArray || function isArray(arg) {
  return cof(arg) == 'Array';
};


/***/ }),

/***/ 720:
/***/ (function(module, exports, __webpack_require__) {

// fallback for IE11 buggy Object.getOwnPropertyNames with iframe and window
var toIObject = __webpack_require__(86);
var gOPN = __webpack_require__(328).f;
var toString = {}.toString;

var windowNames = typeof window == 'object' && window && Object.getOwnPropertyNames
  ? Object.getOwnPropertyNames(window) : [];

var getWindowNames = function (it) {
  try {
    return gOPN(it);
  } catch (e) {
    return windowNames.slice();
  }
};

module.exports.f = function getOwnPropertyNames(it) {
  return windowNames && toString.call(it) == '[object Window]' ? getWindowNames(it) : gOPN(toIObject(it));
};


/***/ }),

/***/ 721:
/***/ (function(module, exports, __webpack_require__) {

var pIE = __webpack_require__(182);
var createDesc = __webpack_require__(178);
var toIObject = __webpack_require__(86);
var toPrimitive = __webpack_require__(326);
var has = __webpack_require__(85);
var IE8_DOM_DEFINE = __webpack_require__(704);
var gOPD = Object.getOwnPropertyDescriptor;

exports.f = __webpack_require__(118) ? gOPD : function getOwnPropertyDescriptor(O, P) {
  O = toIObject(O);
  P = toPrimitive(P, true);
  if (IE8_DOM_DEFINE) try {
    return gOPD(O, P);
  } catch (e) { /* empty */ }
  if (has(O, P)) return createDesc(!pIE.f.call(O, P), O[P]);
};


/***/ }),

/***/ 722:
/***/ (function(module, exports) {



/***/ }),

/***/ 723:
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(181)('asyncIterator');


/***/ }),

/***/ 724:
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(181)('observable');


/***/ }),

/***/ 864:
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var Toc = __webpack_require__(865);

var $ = __webpack_require__(12);

$(document).ready(function () {
  var toc = new Toc();

  toc.render({
    target: document.getElementById('js-toc')
  });
});

/***/ }),

/***/ 865:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_babel_runtime_helpers_typeof__ = __webpack_require__(697);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_babel_runtime_helpers_typeof___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0_babel_runtime_helpers_typeof__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_jquery__ = __webpack_require__(12);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_jquery___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_jquery__);





function Toc(options) {
  var that = this,
      option;

  if ((typeof options === 'undefined' ? 'undefined' : __WEBPACK_IMPORTED_MODULE_0_babel_runtime_helpers_typeof___default()(options)) == 'object') {
    for (option in options) {
      if (option in that) {
        that[option] = options[option];
      }
    }
  }

  that.scope = document.body;
  that.init(options);
}

Toc.prototype = {
  selector: 'h1,h2,h3,h4,h5,h6',

  scope: null,

  from: 1,

  to: 6,

  map: null,

  createLinks: true,

  cssClasses: {
    tocList: 'toc',
    tocListItem: 'toc-item_level_#'
  },

  init: function init(options) {
    var that = this;

    that.map = that.get();
  },

  __getMap: function __getMap(selector, scope) {
    var that = this;
    var sections = [],
        sectionNode,
        section;
    var i, len;
    var selector = typeof selector != 'undefined' ? selector : that.selector;
    var scope = typeof scope != 'undefined' ? scope : that.scope;
    var sectionsNodes = 'querySelectorAll' in scope ? scope.querySelectorAll(selector) : __WEBPACK_IMPORTED_MODULE_1_jquery___default()(selector, scope);

    for (i = 0, len = sectionsNodes.length; i < len; i++) {
      sectionNode = sectionsNodes[i];
      section = {
        id: sectionNode.id,
        level: parseInt(sectionNode.tagName.substr(1, 1)),
        title: __WEBPACK_IMPORTED_MODULE_1_jquery___default()(sectionNode).text(),
        node: sectionNode,
        content: []
      };
      sections.push(section);
    }
    return sections;
  },

  get: function get(options) {
    var that = this;
    var selector = options && typeof options.selector != 'undefined' ? options.selector : that.selector;
    var scope = options && typeof options.scope != 'undefined' ? options.scope : that.scope;
    var sectMap, tocList;

    sectMap = that.__getMap(selector, scope);
    if (sectMap.length == 0) {
      return [];
    }

    tocList = that.__getBranch(sectMap, sectMap[0].level, 0);
    return tocList;
  },

  render: function render(opts) {
    var that = this;
    var from = opts && typeof opts.from != 'undefined' ? opts.from : that.from;
    var to = opts && typeof opts.to != 'undefined' ? opts.to : that.to;
    var target = opts && typeof opts.target != 'undefined' ? opts.target : null;
    var toc;

    toc = that.__create(that.map, from, to);

    if (target != null) {
      target.appendChild(toc);
    }

    return toc;
  },

  __create: function __create(list, from, to) {
    var that = this;
    var createLinks = that.createLinks;
    var ul, li, a, title;
    var section, sectionContent;
    var css = that.cssClasses;
    var i, len;

    if (list.length == 0) {
      return null;
    }

    ul = document.createElement('ul');
    ul.className = css.tocList;
    for (i = 0, len = list.length; i < len; i++) {
      section = list[i];

      li = document.createElement('li');
      li.className = css.tocListItem.replace('#', section.level);

      if (createLinks) {
        title = document.createElement('a');
        title.href = "#" + section.id;
        title.appendChild(document.createTextNode(section.title));
      } else {
        title = document.createTextNode(section.title);
      }

      li.appendChild(title);

      if (section.content.length > 0 && section.content[0].level >= from && section.content[0].level <= to) {
        sectionContent = that.__create(section.content, from, to);
        li.appendChild(sectionContent);
      }

      ul.appendChild(li);
    }
    return ul;
  },

  __getBranch: function __getBranch(sections, level, start, firstRun) {
    var that = this;
    var firstRun = typeof firstRun !== 'undefined' ? firstRun : true;
    var end = sections.length;
    var tree = [];
    var section, prevSect;

    for (var i = start; i < end; i++) {
      section = sections[i];
      prevSect = typeof sections[i - 1] !== 'undefined' ? sections[i - 1] : null;

      if (section.level == level) {
        // siblings
        tree.push(section);
      } else if (section.level > level) {
        // inner branch
        if (prevSect && prevSect.level < section.level) {
          prevSect.content = this.__getBranch(sections, section.level, i, false);
        }
      } else if (section.level < level) {
        // out of branch
        if (!firstRun) {
          break;
        }
      }
    }
    return tree;
  }
};

/* harmony default export */ __webpack_exports__["default"] = (Toc);

/***/ })

},[864]);
//# sourceMappingURL=grammar.js.map