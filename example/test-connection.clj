(use 'org.clojars.rabbitmq)

;; example(1)
(defonce conn-map {:username "guest"
                   :password "guest"
                   :host "localhost"
                   :port 5672
                   :virtual-host "/"
                   :type "direct"
                   :exchange "sorting-room"
                   :queue "po-box"
                   :durable true
                   :routing-key "tata"})

(println "Connecting ...")

(defonce connection (connect conn-map))

(println connection)
