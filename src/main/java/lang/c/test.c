// CTokenizerTermTest.java -----------------------------------------
*100
/100
(1+2)*3/-(4-5)
+4--5++2

// コメントが閉じていないからEOFが来てしまうはず
(1+2)/*3+4

// CTokenizerTermTest.java -----------------------------------------
(1/*+2)/*3+4