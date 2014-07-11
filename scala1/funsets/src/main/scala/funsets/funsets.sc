package funsets
import FunSets._
object funsets {
  printSet(singletonSet(3))                       //> {3}
  
  printSet(union(x=> x < 10 && x > 1, x => x > 5 && x < 12))
                                                  //> {2,3,4,5,6,7,8,9,10,11}
	printSet(diff(x=> x < 10, x => x < 5))    //> {5,6,7,8,9}
  
  printSet(diff(x => x >= 5 && x <= 15, x=> x < 10))
                                                  //> {10,11,12,13,14,15}
  
  exists(x => x > 3 && x < 100, x => x != 15)     //> res0: Boolean = true
  exists(x => x > 3 && x < 100, x => x == 150)    //> res1: Boolean = false
	printSet(map(x=> x > 3 && x < 10, x => x * x))
                                                  //> {16,25,36,49,64,81}
}