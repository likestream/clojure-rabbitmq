(defproject clojure-rabbitmq "0.3.3-SNAPSHOT"
  :description "RabbitMQ client in Clojure."
  :repositories {"mvnrepository" "http://mvnrepository.com/"}
  :main org.clojars.rabbitmq

  :dev-dependencies [[lein-search "0.3.4"]
		     [swank-clojure "1.2.1"]
		     [lein-cdt "1.0.0"]
		     [lein-namespace-depends "0.1.0-SNAPSHOT"]
		     [lein-remote-swank "0.1.0-SNAPSHOT"]]

  :hooks [leiningen.hooks.cdt]
  
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [commons-io "1.4"]
                 [commons-cli "1.2"]
                 [com.rabbitmq/amqp-client "2.1.1"]
                 ; [com.rabbitmq/amqp-client "1.8.1"]
                 ; [com.rabbitmq/amqp-client "1.7.2"]
                 ; [org.clojars.icylisper/rabbitmq-client "1.7.0"]
                 ]
  )
