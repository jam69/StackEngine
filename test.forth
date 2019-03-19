		 'dos'    ( devuelve un dos
		 1 2 +    ( suma 1 y 2)
		 1 2 =    ( una prueba
		 
		 ( un comentario )
		 
		 
		 1 2 = IF 'iguales' THEN
		 1 1 = IF 'iguales' THEN
		 1 1 = IF 'distintos' ELSE 'iguales' THEN .
		 1 2 = IF 'distintos' ELSE 'iguales' THEN .
		 1 DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN  DROP
		 4  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN DROP
		 9  DUP 5 < IF 'mayor de 5' ELSE DUP 3 < IF 'es 4' ELSE 'menor3' THEN THEN DROP
		 : MAS2 2 + ;
		 : MAS3 3 + ;
		 : MAS5 MAS2 MAS3 ;
		 3 MAS2 MAS2 MAS3 MAS5
		 5 MAS5
		 10 0 DO 'hola' . I . LOOP 
		 10 0 DO  10 0 DO '[' . I . J . ']' . LOOP CR  LOOP 
		 10 0 DO 'hola' . I .  CR 2 +LOOP 
		 -10 0 DO I . -1 +LOOP CR 
		: INC-COUNT DO I . DUP +LOOP DROP ; 
		 1 5 0 INC-COUNT
		 2 5 0 INC-COUNT
		 -3 -10  10 INC-COUNT
		32767 1 DO I . I +LOOP
		 uno VARIABLE
		 dos VARIABLE
		 tres VARIABLE
		 cuatro VARIABLE
		 1 uno !
		 2 dos !
		 3 tres !
		 4 cuatro !
		 uno @ dos @ tres @ cuatro @ + + + .
		 uno @ dos @ + tres @ cuatro @  + + .
		 1 tres +!
		 tres ?
		 2 tres +!
		 tres @
		 unArray VARIABLE 10 ALLOT 
		 100 unArray 1 + ! 
		 200 unArray 2 + ! 
		 300 unArray 3 + ! 
		 400 unArray 4 + ! 
		 unArray ?
		 unArray 3 + @ 
		 1000 CONSTANT mil 
		 mil 1 +
		 mil
		 CREATE limits 100 , 200 , 300 , 400 , 
		 limits 3 + ! 
		 limits 3 + . 