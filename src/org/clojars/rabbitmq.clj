(set! *warn-on-reflection* true)

(ns org.clojars.rabbitmq
  (:gen-class)
  (:import (com.rabbitmq.client
;             ConnectionParameters
             Connection
             Channel
             AMQP
             ConnectionFactory
             Consumer
             QueueingConsumer)
           com.rabbitmq.client.QueueingConsumer$Delivery))


;; abbreviatons:
;; ch - channel
;; c  - connection-map
;; q  - queue
;; m  - message
;; d  - delivery

(defn connected? [cm]
  true)

(defn connect [{:keys [username password virtual-host port
                       #^String host]}]
  (let [#^ConnectionFactory cf
        (doto (new ConnectionFactory)
          (.setUsername username)
          (.setPassword password)
          (.setVirtualHost virtual-host)
	  (.setHost host)
	  (.setPort (int port))
          (.setRequestedHeartbeat 0))
        
        #^Connection conn (.newConnection cf)]

    [conn (.createChannel conn)]))

(defn declare-queue
  ([#^Channel ch #^String queue
    durable exclusive auto-delete]
   (declare-queue ch queue durable exclusive auto-delete nil))
  ([#^Channel ch #^String queue
    durable exclusive auto-delete args]
   (.queueDeclare ch queue
                   (boolean durable) (boolean exclusive) (boolean auto-delete)
                   args)))

(defn bind-channel [{:keys [exchange type queue routing-key durable exclusive auto-delete args]}
                   #^Channel ch]
 (try
   (.exchangeDeclare ch exchange type
                     (boolean durable) (boolean auto-delete) 
                     args)
   (declare-queue ch queue durable exclusive auto-delete)
   (.queueBind ch queue exchange routing-key)
   (catch Exception ex
     (.printStackTrace ex))))

(defn publish [{:keys [exchange routing-key]}
               #^Channel ch
               #^String m]
  (let [msg-bytes (.getBytes m)]
    (.basicPublish ch exchange routing-key nil msg-bytes)))


(defn disconnect [#^Connection conn
                  #^Channel ch]
  (.close ch)
  (.close conn))

(defn #^String delivery-contents
  "Return the contents of the Delivery as a String."
  [#^"QueueingConsumer$Delivery" d]
  (when d
    (String. (.getBody d))))

(defn ack-delivery [#^Channel ch
                    #^"QueueingConsumer$Delivery" d]
  "Acknowledge a Delivery from the given Channel."
  (.basicAck ch (.. d getEnvelope getDeliveryTag) false))

(defn delivery-seq
  "Returns a lazy sequence of deliveries from the queue.
   The caller is responsible for acking the message and
   extracting its contents."
  [#^Channel ch
   #^QueueingConsumer q]
  (lazy-seq
   (let [d (.nextDelivery q)]
     (cons d (delivery-seq ch q)))))

;;;; AMQP Queue as a sequence
(defn message-seq
  "Return a lazy sequence of queue items. Automatically
   acks each message and returns the content as a string."
  [#^Channel ch
   #^QueueingConsumer q]
  (lazy-seq
    (let [d (.nextDelivery q)
          m (delivery-contents d)]
      (ack-delivery ch d)
      (cons m (message-seq ch q)))))

(defn #^QueueingConsumer
  declare-queue-and-consumer
  "Return a QueueingConsumer with the appropriate settings."
  ;; TODO: tidy up this signature.
  [#^Channel ch queue prefetch durable exclusive auto-delete]
  (declare-queue ch queue durable exclusive auto-delete)
  (when prefetch
    (.basicQos ch prefetch))
  (QueueingConsumer. ch))

(defmacro with-declared-queue-and-consumer
  "Macro which establishes a declared queue and consumer, starting
   basicConsume, then executing the body.
   `consumer` is bound to the resulting consumer for the duration of the body."
  [[ch consumer options] & body]
  `(let [opts#     ~options
         queue#    (:queue opts#)
         prefetch# (:prefetch opts#)
         durable#     (:durable opts#)
         exclusive#   (:exclusive opts#)
         auto-delete# (:auto-delete opts#)
         cch#      ~ch]
     (let [~consumer
           (declare-queue-and-consumer cch# queue# prefetch#
                                       durable# exclusive# auto-delete#)]
       (.basicConsume cch# queue# ~consumer)
       ~@body)))

(defn queue-delivery-seq
  "Return a sequence of the Delivery objects in the named queue."
  ([#^Channel ch opts]
     (with-declared-queue-and-consumer
       [ch consumer opts]
       (delivery-seq ch consumer))))

(defn queue-message-seq
  "Return a sequence of the messages in queue with name `queue-name`."
  ([#^Channel ch opts]
     (with-declared-queue-and-consumer
       [ch consumer opts]
       (message-seq ch consumer)))
  
  ([conn
    #^Channel ch
    c]
   (queue-message-seq ch c)))

(defn queue-seq [& args]
  (apply queue-message-seq args))

;;; consumer routines
(defn consume-wait
  ([c #^Channel ch {:keys [prefetch durable exclusive auto-delete]}]
     (let [consumer (declare-queue-and-consumer
                     ch (:queue c)
                     prefetch durable exclusive auto-delete)]
       (.basicConsume ch (:queue c) false consumer)
       (while true
              (let [d (.nextDelivery consumer)
                    m (String. (.getBody d))]
                (.basicAck ch (.. d getEnvelope getDeliveryTag) false)
                m))))
  ([c #^Channel ch]
     (consume-wait c ch {})))

(defn consume-poll
  ([c #^Channel ch {:keys [prefetch durable exclusive auto-delete]}]
     (let [consumer (declare-queue-and-consumer
                     ch (:queue c)
                     prefetch durable exclusive auto-delete)]
        (.basicConsume ch (:queue c) false consumer)
        (let [d (.nextDelivery consumer)
              m (String. (.getBody d))]
          m)))
  ([c #^Channel ch]
     (consume-poll c ch {})))
