package funsets
import FunSets._
object funsets {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(77); 
  printSet(singletonSet(3));$skip(64); 
  
  printSet(union(x=> x < 10 && x > 1, x => x > 5 && x < 12));$skip(40); 
	printSet(diff(x=> x < 10, x => x < 5));$skip(56); 
  
  printSet(diff(x => x >= 5 && x <= 15, x=> x < 10));$skip(49); val res$0 = 
  
  exists(x => x > 3 && x < 100, x => x != 15);System.out.println("""res0: Boolean = """ + $show(res$0));$skip(47); val res$1 = 
  exists(x => x > 3 && x < 100, x => x == 150);System.out.println("""res1: Boolean = """ + $show(res$1));$skip(48); 
	printSet(map(x=> x > 3 && x < 10, x => x * x))}
}
