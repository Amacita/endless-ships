(ns endless-ships.views
  (:require [re-frame.core :as rf]
            [endless-ships.subs :as subs]
            [clojure.pprint :refer [pprint]]
            [endless-ships.views.navigation :refer [navigation]]
            [endless-ships.views.ships-list :refer [ships-list]]
            [endless-ships.views.ship-page :refer [ship-page]]
            [endless-ships.views.outfits :refer [outfits]]
            [endless-ships.views.settings :refer [settings-page]]
            [endless-ships.views.outfit-page :refer [outfit-page]]))

(defn current-page []
  (let [[route params] @(rf/subscribe [::subs/route])]
    (case route
      :ships [ships-list]
      :ship [ship-page (:ship/name params) nil]
      :ship-modification [ship-page (:ship/name params) (:ship/modification params)]
      :outfits [outfits]
      :settings [settings-page]
      :outfit [outfit-page (:outfit/name params)]
      [:div (str "Route unknown: " route)])))

(defn interface []
  [:div.container-fluid
   (if @(rf/subscribe [::subs/loading?])
     [:pre "Loading..."]
     (if @(rf/subscribe [::subs/loading-failed?])
       [:pre "Failed to load data."]
       [:div.row
        [:div.col-lg-12
         [navigation]]]))
   [:div.row
    [:div.col-lg-12

     ;; Print a list of all ships that don't have a category defined.
     ;; This is important because a ship with a null category will break the website.
     ;[:pre (with-out-str (pprint (remove :category @(rf/subscribe [::subs/ships]))))]

     [current-page]]]])
