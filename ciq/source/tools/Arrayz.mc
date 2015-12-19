class Arrayz {
   static function addItem(oldArray, newItem) {
      var newArray = new [oldArray.size() + 1];

      for(var i = 0; i < oldArray.size(); i++) {
         newArray[i] = oldArray[i];
      }

       newArray[oldArray.size()] = newItem;

       return newArray;
   }

   static function join(array1, array2) {
      var newArray = new [array1.size() + array2.size()];

      for (var i = 0; i < array1.size(); i++) {
         newArray[i] = array1[i];
      }

      for (var i = 0; i < array2.size(); i++) {
         newArray[i + array1.size()] = array2[i];
      }

      return newArray;
   }
}