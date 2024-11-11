(ns rest-api.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :refer [run-jetty]]))

(def events-url "https://betano.p.rapidapi.com/events?tournamentId=325")
(def header-map {:headers {:x-rapidapi-host "betano.p.rapidapi.com" :x-rapidapi-key "2bd1fb0331msh84b488989710bf2p182a0fjsn8efe5543b0f9"}})

(defn fetch-data []
  (let [resp (client/get events-url header-map)
        body-parsed (json/parse-string (:body resp) true)]
    body-parsed))

(defn games-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string (fetch-data) {:pretty true})})

(defn -main [& args]
  (run-jetty games-handler {:port 8080 :join? false})
  (println "Servidor rodando na porta 8080"))
