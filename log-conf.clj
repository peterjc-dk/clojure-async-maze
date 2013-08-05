{:current-level :debug
 :shared-appender-config
 {:spit-filename "log/log-file.log"}
 :appenders
 {
  :standard-out
  {:min-level nil
   :enabled? true
   :async? false
   :limit-per-msecs nil
   :format-fn :str-edn-short-format-fn}

  :spit
  {:min-level nil
   :enabled? false
   :async? false
   :limit-per-msecs nil
   :format-fn :str-format-fn}

  :spit-edn
  {:min-level nil
   :enabled? true
   :async? false
   :limit-per-msecs nil
   :format-fn :edn-format-fn}}}
