(ns yaclot.core)

; Helpers

(defn to-type 
  ([type] (to-type type {}))
  ([type conversion-params] (assoc conversion-params :type type)))

(defn using-format
  ([format] (using-format format {}))
  ([format conversion-params] (assoc conversion-params :format format)))

; Core

(defmulti convert (fn [val conversion-params] [(class val) (:type conversion-params)]))

(defmacro defconvert 
  ([from-type to-type f] 
    `(defmethod convert [~from-type ~to-type] [val# params#]
       (~f val#)))
  ([from-type to-type f default-conversion-params]
    `(defmethod convert [~from-type ~to-type] [val# params#]
       (~f val# (merge ~default-conversion-params params#)))))

(defn map-convert [in conversion-params]
  (into {}
    (for [[k v] in :let [params (k conversion-params)]]
      (if params
        [k (convert v params)]
        [k v]))))

; Invidicual Converters

(defn parse-date 
  ([str] (parse-date str "yyyy-MM-dd"))
  ([str format] (.parse (java.text.SimpleDateFormat. format) str)))

(defn format-date [dt format]
  (.format (java.text.SimpleDateFormat. format) dt))

(defconvert 
  String
  java.util.Date
  (fn [v conversion-params] (parse-date v (:format conversion-params)))
  (using-format "yyyy-MM-dd"))

(defconvert 
  java.util.Date
  String
  (fn [v conversion-params] (format-date v (:format conversion-params)))
  (using-format "yyyy-MM-dd"))

(defconvert String Integer #(Integer/parseInt %))

(defconvert Integer String #(String/valueOf %))

(defconvert String Double #(Double/parseDouble %))

(defconvert Double String #(String/valueOf %))

(defconvert String BigDecimal #(new BigDecimal %))

(defconvert BigDecimal String #(.toString %))

; TODO: support nil
; TODO: support formatting for numbers
; TODO: error handling