[-][
	Copies any input to output and terminates.
	Will not output a 0 when the stream is finished.
	Uses the first cell as marker whether a byte has
	been written and the second cell for the input. 
]
+
[
	-		Clear marker
	>		Move to input
	,		Read input
	[
		.	Output if input not null
		[-] Clear input
		<+  Mark marker
	>]  	Move to input (which is 0)
<]			Move to marker (only not 0 if input was not 0)