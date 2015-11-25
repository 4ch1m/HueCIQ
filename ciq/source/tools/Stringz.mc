class Stringz {
    static function reverse(string) {
       var reversedString = "";

       for (var i = string.length(); i > 0 ; i--) {
          reversedString = reversedString + string.substring(i-1, i);
       }

       return reversedString;
    }
}
