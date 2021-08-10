(ns endless-ships.views.navigation
  (:require [re-frame.core :as rf]
            [endless-ships.events :as events]
            [endless-ships.subs :as subs]
            [endless-ships.routes :as routes]))

(defn navigation []
  [:nav.navbar.navbar-expand-lg
   {:role :navigation}
   [:div.container-fluid
    [:div.collapse.navbar-collapse {:id :navbarSupportedContent}
     (let [[route] @(rf/subscribe [::subs/route])]
       [:ul.navbar-nav.me-auto.mb-2.mb-lg-0.nav-tabs
        [:li.nav-item {:role :presentation}
         [:a.nav-link {:href (routes/url-for :ships)
                       :aria-current (when (= route :ships) :page)
                       :class (when (= route :ships) :active)}
          "Ships"]]
        [:li.nav-item {:role :presentation}
         [:a.nav-link {:href (routes/url-for :outfits)
                       :aria-current (when (= route :outfits) :page)
                       :class (when (= route :outfits) :active)}
          "Outfits"]]
        [:li.nav-item {:role :presentation}
         [:a.nav-link {:href (routes/url-for :settings)
                       :aria-current (when (= route :settings) :page)
                       :class (when (= route :settings) :active)}
          "Settings"]]])]]])
