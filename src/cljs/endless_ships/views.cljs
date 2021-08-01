(ns endless-ships.views
  (:require [re-frame.core :as rf]
            [endless-ships.subs :as subs]
            [clojure.pprint :refer [pprint]]
            [endless-ships.views.navigation :refer [navigation]]
            [endless-ships.views.ships-list :refer [ships-list ships-filter]]
            [endless-ships.views.ship-page :refer [ship-page]]
            [endless-ships.views.outfits :refer [outfits]]
            [endless-ships.views.outfit-page :refer [outfit-page]]))

(defn current-page []
  (let [[route params] @(rf/subscribe [::subs/route])]
    (case route
      :ships [ships-list]
      :ship [ship-page (:ship/name params) nil]
      :ship-modification [ship-page (:ship/name params) (:ship/modification params)]
      :outfits [outfits]
      :outfit [outfit-page (:outfit/name params)]
      [:div (str "Route unknown: " route)])))

(defn gw-version []
  (let [{:keys [hash date tag]} @(rf/subscribe [::subs/gw-version])]
    [:div.game-version
     (if (some? tag)
       [:a {:href (str "https://github.com/1010todd/galactic-war/releases/tag/" tag)
            :target :blank}
        "Galactic War " tag]
       [:a {:href (str "https://github.com/1010todd/galactic-war/commit/" hash)
            :target :blank}
        "galactic-war@" (subs hash 0 7)])
     " (" date ")"]))

(defn game-version []
  (let [{:keys [hash date tag]} @(rf/subscribe [::subs/game-version])]
    [:div.game-version
     (if (some? tag)
       [:a {:href (str "https://github.com/endless-sky/endless-sky/releases/tag/" tag)
            :target :blank}
        "Endless Sky " tag]
       [:a {:href (str "https://github.com/endless-sky/endless-sky/commit/" hash)
            :target :blank}
        "endless-sky@" (subs hash 0 7)])
     " (" date ")"]))

(defn interface []
  [:div.container-fluid
   [:div.row
    [:div.col-lg-12
     (if @(rf/subscribe [::subs/loading?])
       [:div.app "Loading..."]
       (if @(rf/subscribe [::subs/loading-failed?])
         [:div.app "Failed to load data."]
         [:div.app
          [game-version]
          [gw-version]
          ;[:pre "GW Version " (with-out-str (print @(rf/subscribe [::subs/gw-version])))]
          ;[:pre "Vanilla Version " (with-out-str (print @(rf/subscribe [::subs/game-version])))]
          [navigation]
          [:pre (str "Debug: " (with-out-str (print @(rf/subscribe [::subs/debug]))))]

          ;[:pre (with-out-str (pprint @(rf/subscribe [::subs/license-labels])))]
          ;[:pre (with-out-str (pprint @(rf/subscribe [::subs/ships-race-filter])))]
          ;[:pre (with-out-str (pprint @(rf/subscribe [::subs/ships-category-filter])))]
          ;[:pre (with-out-str (pprint @(rf/subscribe [::subs/ships-license-filter])))]
          ;[:pre (with-out-str (pprint @(rf/subscribe [::subs/ships])))]
          ;[:pre (with-out-str (pprint @(rf/subscribe [::subs/license-label "Navy"])))]

          ;; Print a list of all ships that don't have a category defined.
          ;; This is important because a ship with a null category will break the website.
          ;[:pre (with-out-str (pprint (remove :category @(rf/subscribe [::subs/ships]))))]

          [current-page]
          ]))
     ]]])
