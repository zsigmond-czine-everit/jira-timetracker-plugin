/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Taylor Hakes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
(function (main) {
  'use strict';

  /**
   * Parse or format dates
   * @class fecha
   */
  var fecha = {},
    token = /d{1,4}|M{1,4}|YY(?:YY)?|S{1,3}|Do|ZZ|([HhMsDm])\1?|[aA]|"[^"]*"|'[^']*'/g,
    dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
    monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
    amPm = ['am', 'pm'],
    twoDigitsDays = /\d{2}/, twoDigits = /\d{2}/, threeDigits = /\d{3}/, fourDigits = /\d{4}/,
    wordForMonth = /([0-9]{1,2}['a-z\u00A0-\u05FF\u0700-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF\u002E]+|[\u0600-\u06FF\/]+(\s*?[\u0600-\u06FF]+){1,2})|([0-9]{1,2})|(['a-z\u00A0-\u05FF\u0700-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF\u002E]+|[\u0600-\u06FF\/]+(\s*?[\u0600-\u06FF]+){1,2})/i,
    word = /[0-9]*['a-z\u00A0-\u05FF\u0700-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF\u002E]+|[\u0600-\u06FF\/]+(\s*?[\u0600-\u06FF]+){1,2}/i,
    noop = function () {},
    dayNamesShort, monthNamesShort,
    parseFlags = {
      D: [twoDigitsDays, function (d, v) {
        d.day = v;
      }],
      M: [twoDigits, function (d, v) {
        d.month = v - 1;
      }],
      YY: [twoDigits, function (d, v) {
        var da = new Date(), cent = +('' + da.getFullYear()).substr(0, 2);
        d.year = '' + (v > 68 ? cent - 1 : cent) + v;
      }],
      h: [twoDigits, function (d, v) {
        d.hour = v;
      }],
      m: [twoDigits, function (d, v) {
        d.minute = v;
      }],
      s: [twoDigits, function (d, v) {
        d.second = v;
      }],
      YYYY: [fourDigits, function (d, v) {
        d.year = v;
      }],
      S: [/\d/, function (d, v) {
        d.millisecond = v * 100;
      }],
      SS: [/\d{2}/, function (d, v) {
        d.millisecond = v * 10;
      }],
      SSS: [threeDigits, function (d, v) {
        d.millisecond = v;
      }],
      d: [twoDigits, noop],
      ddd: [word, noop],
      MMM: [wordForMonth, monthUpdate('monthNamesShort')],
      MMMM: [wordForMonth, monthUpdate('monthNames')],
      a: [word, function (d, v) {
        var val = v.toLowerCase();
        if (val === amPm[0]) {
          d.isPm = false;
        } else if (val === amPm[1]) {
          d.isPm = true;
        }
      }],
      ZZ: [/[\+\-]\d\d:?\d\d/, function (d, v) {
        var parts = (v + '').match(/([\+\-]|\d\d)/gi), minutes;

        if (parts) {
          minutes = +(parts[1] * 60) + parseInt(parts[2], 10);
          d.timezoneOffset = parts[0] === '+' ? minutes : -minutes;
        }

      }]
    };
  parseFlags.dd = parseFlags.d;
  parseFlags.dddd = parseFlags.ddd;
  parseFlags.Do = parseFlags.DD = parseFlags.D;
  parseFlags.mm = parseFlags.m;
  parseFlags.hh = parseFlags.H = parseFlags.HH = parseFlags.h;
  parseFlags.MM = parseFlags.M;
  parseFlags.ss = parseFlags.s;
  parseFlags.A = parseFlags.a;

  monthNamesShort = shorten(monthNames, 3);
  dayNamesShort = shorten(dayNames, 3);

  function monthUpdate(arrName) {
    return function (d, v) {
      var lowerArray = fecha.i18n[arrName].slice(0);
      lowerArray.forEach(function(part, index, theArray) {
         theArray[index] = theArray[index].toLowerCase();
      });
      var index = lowerArray.indexOf(v.toLowerCase());
      if (~index) {
        d.month = index;
      }
    }
  }

  function pad(val, len) {
    val = String(val);
    len = len || 2;
    while (val.length < len) {
      val = '0' + val;
    }
    return val;
  }

  function shorten(arr, sLen) {
    var newArr = [];
    for (var i = 0, len = arr.length; i < len; i++) {
      newArr.push(arr[i].substr(0, sLen));
    }
    return newArr;
  }

  function DoFn(D) {
    return D + ['th', 'st', 'nd', 'rd'][D % 10 > 3 ? 0 : (D - D % 10 !== 10) * D % 10];
  }

  fecha.i18n = {
    dayNamesShort: dayNamesShort,
    dayNames: dayNames,
    monthNamesShort: monthNamesShort,
    monthNames: monthNames,
    amPm: amPm,
    DoFn: DoFn
  };

  // Some common format strings
  fecha.masks = {
    'default': 'ddd MMM DD YYYY HH:mm:ss',
    shortDate: 'M/D/YY',
    mediumDate: 'MMM D, YYYY',
    longDate: 'MMMM D, YYYY',
    fullDate: 'dddd, MMMM D, YYYY',
    shortTime: 'HH:mm',
    mediumTime: 'HH:mm:ss',
    longTime: 'HH:mm:ss.SSS'
  };

  /***
   * Format a date
   * @method format
   * @param {Date|number} dateObj
   * @param {string} mask Format of the date, i.e. 'mm-dd-yy' or 'shortDate'
   */
  fecha.format = function (dateObj, mask) {
    if (typeof dateObj === 'number') {
      dateObj = new Date(dateObj);
    }

    if (Object.prototype.toString.call(dateObj) !== '[object Date]' || isNaN(dateObj.getTime())) {
      throw new Error('Invalid Date in fecha.format');
    }

    mask = fecha.masks[mask] || mask || fecha.masks['default'];

    var D = dateObj.getDate(),
      d = dateObj.getDay(),
      M = dateObj.getMonth(),
      y = dateObj.getFullYear(),
      H = dateObj.getHours(),
      m = dateObj.getMinutes(),
      s = dateObj.getSeconds(),
      S = dateObj.getMilliseconds(),
      o = dateObj.getTimezoneOffset(),
      flags = {
        D: D,
        DD: pad(D),
        Do: fecha.i18n.DoFn(D),
        d: d,
        dd: pad(d),
        ddd: fecha.i18n.dayNamesShort[d],
        dddd: fecha.i18n.dayNames[d],
        M: M + 1,
        MM: pad(M + 1),
        MMM: fecha.i18n.monthNamesShort[M],
        MMMM: fecha.i18n.monthNames[M],
        YY: String(y).slice(2),
        YYYY: y,
        h: H % 12 || 12,
        hh: pad(H % 12 || 12),
        H: H,
        HH: pad(H),
        m: m,
        mm: pad(m),
        s: s,
        ss: pad(s),
        S: Math.round(S / 100),
        SS: pad(Math.round(S / 10), 2),
        SSS: pad(S, 3),
        a: H < 12 ? fecha.i18n.amPm[0] : fecha.i18n.amPm[1],
        A: H < 12 ? fecha.i18n.amPm[0].toUpperCase() : fecha.i18n.amPm[1].toUpperCase(),
        ZZ: (o > 0 ? '-' : '+') + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4)
      };

    return mask.replace(token, function ($0) {
      return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
    });
  };

  /**
   * Parse a date string into an object, changes - into /
   * @method parse
   * @param {string} dateStr Date string
   * @param {string} format Date parse format
   * @returns {Date|boolean}
   */
  fecha.parse = function (dateStr, format) {
    var isValid, dateInfo, today, date, info, index;

    if (typeof format !== 'string') {
      throw new Error('Invalid format in fecha.parse');
    }

    format = fecha.masks[format] || format;

    // Avoid regular expression denial of service, fail early for really long strings
    // https://www.owasp.org/index.php/Regular_expression_Denial_of_Service_-_ReDoS
    if (dateStr.length > 1000) {
      return false;
    }

    isValid = true;
    dateInfo = {};
    format.replace(token, function ($0) {
      if (parseFlags[$0]) {
        info = parseFlags[$0];
        index = dateStr.search(info[0]);
        if (!~index) {
          isValid = false;
        } else {
          dateStr.replace(info[0], function (result) {
            info[1](dateInfo, result);
            dateStr = dateStr.substr(index + result.length);
            return result;
          });
        }
      }

      return parseFlags[$0] ? '' : $0.slice(1, $0.length - 1);
    });

    if (!isValid) {
      return false;
    }

    today = new Date();
    if (dateInfo.isPm === true && dateInfo.hour != null && +dateInfo.hour !== 12) {
      dateInfo.hour = +dateInfo.hour + 12;
    } else if (dateInfo.isPm === false && +dateInfo.hour === 12) {
      dateInfo.hour = 0;
    }
    if(typeof dateInfo.month == 'undefined'){
      return false;
    }
    if(!dateInfo.day){
      return false;
    }
    if (dateInfo.timezoneOffset != null) {
      dateInfo.minute = +(dateInfo.minute || 0) - +dateInfo.timezoneOffset;
      date = new Date(Date.UTC(dateInfo.year || today.getFullYear(), dateInfo.month || 0, dateInfo.day || 1,
        dateInfo.hour || 0, dateInfo.minute || 0, dateInfo.second || 0, dateInfo.millisecond || 0));
    } else {
      date = new Date(dateInfo.year || today.getFullYear(), dateInfo.month || 0, dateInfo.day || 1,
        dateInfo.hour || 0, dateInfo.minute || 0, dateInfo.second || 0, dateInfo.millisecond || 0);
    }
    return date;
  };

  /* istanbul ignore next */
  if (typeof module !== 'undefined' && module.exports) {
    module.exports = fecha;
  } else if (typeof define === 'function' && define.amd) {
    define(function () {
      return fecha;
    });
  } else {
    main.fecha = fecha;
  }
})(this);
