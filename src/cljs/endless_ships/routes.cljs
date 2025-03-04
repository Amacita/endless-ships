(ns endless-ships.routes
  (:require [re-frame.core :as rf]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [endless-ships.events :as events]
            [endless-ships.views.utils :refer [kebabize nbspize]]))

(def name-pattern
  #"[A-Za-z0-9\-\(\)\,\"]+")

(def routes
  ["/gw-endless-ships/" {"" :ships
                         ["ships/" :ship/name] :ship
                         ["ships/" :ship/name "/" [name-pattern :ship/modification]] :ship-modification
                         "outfits" :outfits
                         "settings" :settings
                         ["outfits/" [name-pattern :outfit/name]] :outfit}])

(defn- parse-url [url]
  (let [{:keys [handler route-params]
         :or {route-params {}}}
        (bidi/match-route routes url)]
    [handler route-params]))

(defn- dispatch-route [matched-route]
  (if (some? js/window.ga)
    (js/window.ga "send" "pageview" js/location.pathname))
  (rf/dispatch [::events/navigate-to matched-route]))

(def url-for
  (partial bidi/path-for routes))

(defn start! []
  (let [history (pushy/pushy dispatch-route parse-url)]
    (pushy/start! history)))

(defn ship-link [name]
  (let [url (url-for :ship :ship/name (kebabize name))]
    [:a {:href url} (nbspize name)]))

(defn ship-url [name]
  (url-for :ship :ship/name (kebabize name)))

(defn ship-modification-link [name modification]
  (let [url (url-for :ship-modification :ship/name (kebabize name) :ship/modification (kebabize modification))]
    [:a {:href url} (nbspize modification)]))

(defn ship-modification-url [name modification]
  (url-for :ship-modification :ship/name (kebabize name) :ship/modification (kebabize modification)))

(defn outfit-link [name]
  (let [url (url-for :outfit :outfit/name (kebabize name))]
    [:a {:href url} (nbspize name)]))
