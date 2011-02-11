(ns rabbitmq-publisher
  (:require [org.clojars.rabbitmq :as rabbitmq]))

(defonce conn-map {:username "guest"
                   :password "guest"
                   :host "localhost"
                   :port 5672
                   :virtual-host "/"
                   :type "direct"
                   :exchange "sorting-room"
                   :queue "po-box"
                   :durable true
                   :exclusive false :auto-delete false
                   :args nil
                   :routing-key "tata"})

(println conn-map)

(let [[conn chan] (rabbitmq/connect conn-map)]
  (defonce connection conn)
  (defonce channel chan))

(println connection)

(def c (ref 0))


(while true
  (dosync (alter c inc))
  (println "cycle: " @c)

  ;; publish
  (do
    ;;(swank.core/break)
    (rabbitmq/bind-channel conn-map channel)
    (println "rabbitmq publishing:" (format "message%d" @c))
    (rabbitmq/publish conn-map channel (format "message%d" @c))))
  
  (Thread/sleep 1000))
