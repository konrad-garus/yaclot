# yaclot

Yaclot is a light Clojure conversion library. Use it to convert, parse and format values or records of different types. 

## Usage

### Simple conversions:

    (convert "2011-02-12" (to-type java.util.Date))
    ; => #<Date Sat Feb 12 00:00:00 CET 2011>

    (convert (java.util.Date. 111 1 12) (to-type String))
    ; => "2011-02-12"

    (convert 42 (to-type String))
    ; => "42"

    (convert "42e-2" (to-type Number))
    ; => 0.42M

### Conversions with specified format:

    (convert "2/12/11" (using-format "M/dd/yy" (to-type java.util.Date)))
    ; => #<Date Sat Feb 12 00:00:00 CET 2011>
    
    ; This one attempts parsing with each of the formats and returns first result
    ; which didn't throw ParseException
    (convert "2/12/11" (using-format ["yyyy-MM-dd" "M/dd/yy"] (to-type java.util.Date)))
    ; => #<Date Sat Feb 12 00:00:00 CET 2011>

    (convert 5000.42 (to-type String (using-format "%,.2f")))
    ; => "5,000.42"

### Bulk-convert maps:

    (map-convert 
      {:dt "2011-02-12" :int 42} 
      {:dt (to-type java.util.Date) 
       :int (to-type String)})
    ; => {:dt #<Date Sat Feb 12 00:00:00 CET 2011>, :int "42"}

## Supported Conversions

    String <-> Date
    String <-> Integer
    String <-> Double
    String <-> BigDecimal
    String <-> Number (using BigDecimal)
    Date   <-> Long 
    nil     -> (anything) gives nil

## Planned features

Error handling with local bindings rather than throwing exceptions.

Pre-Validation: Assertions on value before conversion (e.g. checking that it's not null or matches a regular expression).

Post-Validation: Assertions after conversion (e.g. checking that parsed number is positive). 

## License

Distributed under the Eclipse Public License, the same as Clojure.