(ns yaclot.core)

; Helpers
(def format-aliases
  {:iso-date "yyyy-MM-dd"
   :iso-date-time "yyyy-MM-dd'T'HH:mm:ssX"
   :iso-time-hh-mm "HH:mm"
   :iso-time-hh-mm-ss "HH:mm:ss"})

(defn to-type 
  ([type] (to-type type {}))
  ([type conversion-params] (assoc conversion-params :type type)))

(defn using-format
  ([format] 
    (using-format format {}))
  ([format conversion-params] 
    (assoc conversion-params :format (if (keyword? format) (format-aliases format) format))))

(defn in-timezone
  ([zone] (in-timezone zone {}))
  ([zone conversion-params] (assoc conversion-params :timezone zone)))

; Core

(defmulti convert (fn [val conversion-params] [(class val) (:type conversion-params)]))

(defmacro defconvert 
  ([from-type to-type f] 
    `(do
       (defmethod convert [~from-type ~to-type] [val# params#]
         (~f val#))
       (defmethod convert [~from-type ~from-type] [val# params#] (identity val#))
       (defmethod convert [~to-type ~to-type] [val# params#] (identity val#))
       ))
  ([from-type to-type f default-conversion-params]
    `(do
       (defmethod convert [~from-type ~to-type] [val# params#]
         (~f val# (merge ~default-conversion-params params#)))
       (defmethod convert [~from-type ~from-type] [val# params#] (identity val#))
       (defmethod convert [~to-type ~to-type] [val# params#] (identity val#)))))

(defn map-convert [in conversion-params]
  (into {}
    (for [[k v] in :let [params (k conversion-params)]]
      (if params
        [k (convert v params)]
        [k v]))))

; Individual Converters

(defn parse-date
  ([value] (parse-date value {}))
  ([value {:keys [format timezone] 
           :or {format "yyyy-MM-dd" 
                timezone (java.util.TimeZone/getDefault)}}]
    (let [timezone (if (string? timezone) (java.util.TimeZone/getTimeZone timezone) timezone)]
      (if (sequential? format)
        (if-let [res (first (filter identity (map #(try (parse-date value {:format % :timezone timezone}) (catch java.text.ParseException e)) format)))]
          res
          (throw (java.text.ParseException. (str "Unparseable date: " value) 0)))
        (let [fmt (java.text.SimpleDateFormat. format)
              _ (.setTimeZone fmt timezone)]
          (.parse fmt value))))))

(defn format-date [value {:keys [format timezone] 
                          :or {format "yyyy-MM-dd" 
                               timezone (java.util.TimeZone/getDefault)}}]
  (let [fmt (java.text.SimpleDateFormat. format)
        _ (.setTimeZone fmt timezone)]
    (.format fmt value)))

(defconvert 
  String
  java.util.Date
  (fn [v conversion-params] (parse-date v conversion-params))
  (using-format "yyyy-MM-dd"))

(defconvert 
  java.util.Date
  String
  (fn [v conversion-params] (format-date v conversion-params))
  (using-format "yyyy-MM-dd"))

(defconvert 
  String 
  java.sql.Timestamp
  (fn [v conversion-params]
    (let [^java.util.Date dt (convert v (to-type java.util.Date conversion-params))]
      (java.sql.Timestamp. (.getTime dt))))
  {})

(defconvert 
  String 
  java.sql.Time
  (fn [v conversion-params]
    (let [^java.util.Date dt (convert v (to-type java.util.Date conversion-params))]
      (java.sql.Time. (.getTime dt))))
  (using-format :iso-date-time (in-timezone "UTC")))

(defconvert 
  String 
  java.sql.Date
  (fn [v conversion-params]
    (let [^java.util.Date dt (convert v (to-type java.util.Date conversion-params))]
      (java.sql.Date. (.getTime dt))))
  {})

(defconvert java.util.Date Long #(.getTime ^java.util.Date %))

(defconvert Long java.util.Date #(java.util.Date. ^Long %))

(defconvert String Integer #(Integer/parseInt %))

(defconvert String Double #(Double/parseDouble %))

(defconvert String Long #(Long/parseLong %))

(defconvert String BigDecimal #(BigDecimal. ^String %))

(defconvert String Number #(BigDecimal. ^String %))

(defconvert String Boolean #(Boolean/valueOf ^String %))

(defn format-number [n fmt]
  (if fmt
    (format fmt n)
    (str n)))

(defconvert
  Number
  String
  (fn [v conversion-params] (format-number v (:format conversion-params)))
  nil)

(defconvert nil Object (fn [_]))

; Aliases

(defn defalias 
  ([type alias]
    (defalias type alias []))
  ([type alias convertible-from]
    (defconvert type alias identity)
    (defconvert alias type identity)
    (dorun (map #(defconvert % alias (fn [v params] (convert v (to-type type params))) {}) convertible-from))))

(defalias String :string [Number java.util.Date])
(defalias Boolean :boolean [String])
(defalias java.util.Date :date [String])
(defalias Integer :integer [String])
(defalias BigDecimal :decimal [String])
(defalias Double :double [String])
(defalias Number :number [String])
(defalias Long :long [String])
(defalias java.sql.Time :sql-time [String])
(defalias java.sql.Timestamp :sql-timestamp [String])
(defalias java.sql.Date :sql-date [String])

(defconvert Long :date (fn [v] (convert v (to-type java.util.Date))))
(defconvert java.util.Date :long (fn [v] (convert v (to-type Long))))
