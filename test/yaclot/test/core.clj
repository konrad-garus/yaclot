(ns yaclot.test.core
  (:use [clojure.test])
  (:use [yaclot.core]))

(deftest test-string-to-date
  (let [expect (parse-date "2011-02-12")]
    (is (= expect (convert "2011-02-12" (to-type java.util.Date))))
    (is (= expect (convert "2/12/11" (using-format "M/dd/yy" (to-type java.util.Date)))))
    (is (= expect (convert "2/12/11" (using-format ["yyyy-M-d" "M/dd/yy"] (to-type java.util.Date)))))
    (is (= expect (convert "2/12/11" (using-format ["yyyy-M-d" "M/dd/yy"] (to-type :date)))))))

(deftest test-string-to-sql-timestamp
  (let [expect (java.sql.Timestamp. (.getTime (parse-date "2011-02-12")))]
    (is (= expect (convert "2011-02-12" (to-type java.sql.Timestamp))))
    (is (= expect (convert "2/12/11" (using-format "M/dd/yy" (to-type java.sql.Timestamp)))))
    (is (= expect (convert "2/12/11" (using-format "M/dd/yy" (to-type :sql-timestamp)))))))

(deftest test-string-to-sql-date
  (let [expect (java.sql.Date. (.getTime (parse-date "2011-02-12")))]
    (is (= expect (convert "2011-02-12" (to-type java.sql.Date))))
    (is (= expect (convert "2/12/11" (using-format "M/dd/yy" (to-type java.sql.Date)))))
    (is (= expect (convert "2/12/11" (using-format "M/dd/yy" (to-type :sql-date)))))))

(deftest test-string-to-sql-time
  "X format support JDK 7+"
  (let [expect (java.sql.Time. (.getTime (parse-date "1970-01-01T10:00:00Z" {:format "yyyy-MM-dd'T'HH:mm:ssX"})))]
    (is (= expect (convert "1970-01-01T10:00:00Z" (to-type java.sql.Time))))
    (is (= expect (convert "10:00" (using-format "HH:mm" (to-type java.sql.Time)))))
    (is (= expect (convert "10:00" (using-format "HH:mm" (to-type :sql-time)))))))

(deftest date-format-aliases
  (let [expect (parse-date "2013-03-01T10:00:00" {:format "yyyy-MM-dd'T'HH:mm:ss" :timezone "UTC"})]
    (is (= expect (convert "2013-03-01T10:00:00Z" (using-format :iso-date-time (to-type :date))))))
  (let [expect (parse-date "2013-03-01" {:format "yyyy-MM-dd"})]
    (is (= expect (convert "2013-03-01" (using-format :iso-date (to-type :date))))))
  (let [expect (parse-date "1970-01-01T10:00:00" {:format "yyyy-MM-dd'T'HH:mm:ss"})]
    (is (= expect (convert "10:00" (using-format :iso-time-hh-mm (to-type :date))))))
  (let [expect (parse-date "1970-01-01T10:20:30" {:format "yyyy-MM-dd'T'HH:mm:ss"})]
    (is (= expect (convert "10:20:30" (using-format :iso-time-hh-mm-ss (to-type :date)))))))
  
(deftest test-date-to-string
  (let [dt (parse-date "2011-02-12")]
    (is (= "2011-02-12" (convert dt (to-type String))))
    (is (= "2/12/11"(convert dt (using-format "M/dd/yy" (to-type String)))))
    (is (= "2/12/11"(convert dt (using-format "M/dd/yy" (to-type :string)))))))

(deftest test-date-to-long
  (let [dt (parse-date "2011-02-12")]
    (is (= (.getTime dt) (convert dt (to-type Long))))
    (is (= (.getTime dt) (convert dt (to-type :long))))))

(deftest test-long-to-date
  (let [dt (parse-date "2011-02-12")]
    (is (= dt (convert (.getTime dt) (to-type java.util.Date))))
    (is (= dt (convert (.getTime dt) (to-type :date))))))

(deftest test-string-to-integer
  (is (= 1 (convert "1" (to-type Integer))))
  (is (= 1 (convert "1" (to-type :integer)))))

(deftest test-string-to-double
  (is (= (double 1) (convert "1" (to-type Double))))
  (is (= (double 0.3) (convert "0.3" (to-type Double))))
  (is (= (double 0.3) (convert "0.3" (to-type :double)))))

(deftest test-string-to-big-decimal
  (is (= (BigDecimal. 1) (convert "1" (to-type BigDecimal))))
  (is (= (BigDecimal. "0.33") (convert "0.33" (to-type BigDecimal))))
  (is (= (BigDecimal. "0.33") (convert "0.33" (to-type :decimal)))))

(deftest test-string-long
  (is (= (Long. 1) (convert "1" (to-type Long))))
  (is (= (Long. "33") (convert "33" (to-type Long))))
  (is (= (Long. "10000000000") (convert "10000000000" (to-type :long)))))

(deftest test-integer-to-string
  (is (= "1000" (convert 1000 (to-type String))))
  (is (= "1000" (convert 1000 (to-type :string)))))

(deftest test-double-to-string
  (is (= "0.3" (convert (double 0.3) (to-type String))))
  (is (= "0.3" (convert (double 0.3) (to-type :string)))))

(deftest test-big-decimal-to-string
  (is (= "0.3" (convert (BigDecimal. "0.3") (to-type String))))
  (is (= "0.3" (convert (BigDecimal. "0.3") (to-type :string)))))

(deftest test-string-to-number
  (is (= 0.42M (convert "0.42" (to-type Number))))
  (is (= 0.42M (convert "42e-2" (to-type Number))))
  (is (= 1M (convert "1" (to-type Number))))
  (is (= 1M (convert "1" (to-type :number)))))

(deftest test-format-number
  (is (= "5,000.42" (convert 5000.42 (to-type String (using-format "%,.2f"))))) 
  (is (= "5,000" (convert 5000 (to-type String (using-format "%,d"))))))

(deftest test-string-to-boolean
  (is (= false (convert "false" (to-type Boolean))))
  (is (= false (convert "nope" (to-type Boolean))))
  (is (= true (convert "true" (to-type Boolean))))
  (is (= true (convert "true" (to-type :boolean)))))

(deftest test-aliases
  (is (= false (convert false (to-type :boolean))))
  (is (= "str" (convert "str" (to-type :string)))))

(deftest test-convert-nil
  (is (nil? (convert nil (to-type Number))))
  (is (nil? (convert nil (to-type String)))))

(deftest test-map-convert
  (let [dt (parse-date "2011-02-12")
        fmt {:d1 (to-type java.util.Date) 
             :d2 (using-format "M/dd/yy" (to-type String))}
        in {:d1 "2011-02-12"
            :d2 dt
            :d3 "Something else"}]
    (is (= 
          {:d1 dt :d2 "2/12/11" :d3 "Something else"}
          (map-convert in fmt)))))

(deftest test-map-convert-leaves-unspecified-intact
  (is (= 
        {:a 15 :b "Test"}
        (map-convert {:a "15" :b "Test"} {:a (to-type Integer)}))))

(deftest test-convert-identity
  (is (= "Test" (convert "Test" (to-type String)) )))
