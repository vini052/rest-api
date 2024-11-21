(ns rest-api.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]))

;; URLs e Headers
(def events-url "https://betano.p.rapidapi.com/events?tournamentId=325")
(def odds-url "https://betano.p.rapidapi.com/odds_betano?eventId=%s&oddsFormat=decimal&raw=false")
(def header-map {:headers {:x-rapidapi-host "betano.p.rapidapi.com"
                           :x-rapidapi-key "6e1c3a31f3msh06fcd8a735d6399p1499cfjsn93f34b001820"}})

;; Função para buscar informações dos jogos
(defn fetch-games []
  (let [response (client/get events-url header-map)
        body-parsed (json/parse-string (:body response) true)]
    (:events body-parsed)))

;; Função para buscar odds de um jogo por ID
(defn fetch-odds [event-id]
  (let [resp (client/get (format odds-url event-id) header-map)
        body-parsed (json/parse-string (:body resp) true)
        results-odd (get-in body-parsed [:markets :101 :outcomes])
        btts-odd (get-in body-parsed [:markets :104 :outcomes])]
    {:results [(get-in results-odd [:101 :bookmakers :betano :price])
               (get-in results-odd [:102 :bookmakers :betano :price])
               (get-in results-odd [:103 :bookmakers :betano :price])]
     :btts [(get-in btts-odd [:104 :bookmakers :betano :price])
            (get-in btts-odd [:105 :bookmakers :betano :price])]}))

;; Função para combinar dados dos jogos com odds
(defn combine-data []
  (let [games (fetch-games)]
    (map (fn [game]
           (let [event-id (:eventId game)
                 odds (fetch-odds event-id)]
             (assoc game :odds odds)))
         (vals games))))

;; Handler do servidor para fornecer dados unificados
(defn games-handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string (combine-data) {:pretty true})})

(def app
  (wrap-cors games-handler
             :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :post :put :delete]
             :access-control-allow-headers ["Content-Type" "Authorization"]))(def app
                                                                               (wrap-cors games-handler
                                                                                          :access-control-allow-origin [#".*"]
                                                                                          :access-control-allow-methods [:get :post :put :delete]
                                                                                          :access-control-allow-headers ["Content-Type" "Authorization"]))


(defn -main [& args]
  (run-jetty app {:port 8080 :join? false})
  (println "Servidor unificado rodando na porta 8080"))