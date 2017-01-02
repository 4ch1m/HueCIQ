class Stringz {
    static function reverse(string) {
       var reversedString = "";

       for (var i = string.length(); i > 0 ; i--) {
          reversedString = reversedString + string.substring(i-1, i);
       }

       return reversedString;
    }

    static function split(string, separator) {
       var workString = string;
       var token;
       var findIndex = workString.find(separator);
       var resultArray = new [0];

       while (findIndex != null) {
          token = workString.substring(0, findIndex);
          resultArray = Arrayz.addItem(resultArray, token);
          workString = workString.substring(findIndex + 1, workString.length());
          findIndex = workString.find(separator);
       }

       resultArray = Arrayz.addItem(resultArray, workString);

       return resultArray;
    }

    static function join(strings, separator) {
        var joined = strings[0];

        for (var i=1; i < strings.size(); i++) {
            joined = joined + separator + strings[i];
        }

        return joined;
    }

    static function wrap(string) {
        var split = Stringz.split(string, Constantz.BLANK);
        var joined = Stringz.join(split, Constantz.NEW_LINE);

        return joined;
    }
}
