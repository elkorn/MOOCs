object sheet {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(180); 
  def pascal(col: Int, row: Int): Int =
    if (row == 0 || row == 1 || col == 0 || col == row)
      1
    else
      pascal(col - 1, row - 1) + pascal(col, row -1);System.out.println("""pascal: (col: Int, row: Int)Int""");$skip(18); val res$0 = 
  
  pascal(0, 0);System.out.println("""res0: Int = """ + $show(res$0));$skip(15); val res$1 = 
  pascal(0, 1);System.out.println("""res1: Int = """ + $show(res$1));$skip(15); val res$2 = 
  pascal(1, 1);System.out.println("""res2: Int = """ + $show(res$2));$skip(15); val res$3 = 
  pascal(0, 2);System.out.println("""res3: Int = """ + $show(res$3));$skip(15); val res$4 = 
  pascal(1, 2);System.out.println("""res4: Int = """ + $show(res$4));$skip(15); val res$5 = 
  pascal(2, 2);System.out.println("""res5: Int = """ + $show(res$5));$skip(15); val res$6 = 
  pascal(0, 3);System.out.println("""res6: Int = """ + $show(res$6));$skip(15); val res$7 = 
  pascal(1, 3);System.out.println("""res7: Int = """ + $show(res$7));$skip(15); val res$8 = 
  pascal(2, 3);System.out.println("""res8: Int = """ + $show(res$8));$skip(15); val res$9 = 
  pascal(3, 3);System.out.println("""res9: Int = """ + $show(res$9));$skip(15); val res$10 = 
  pascal(0, 4);System.out.println("""res10: Int = """ + $show(res$10));$skip(15); val res$11 = 
  pascal(1, 4);System.out.println("""res11: Int = """ + $show(res$11));$skip(15); val res$12 = 
  pascal(2, 4);System.out.println("""res12: Int = """ + $show(res$12));$skip(15); val res$13 = 
  pascal(3, 4);System.out.println("""res13: Int = """ + $show(res$13));$skip(15); val res$14 = 
  pascal(4, 4);System.out.println("""res14: Int = """ + $show(res$14))}
}
