(ns endless-ships.views.settings
  (:require [re-frame.core :as rf]
            [endless-ships.utils.filters :as filters]
            [endless-ships.subs :as subs]))

(defn settings-page []
  [:div.app
   [:pre "Settings page"]
   [filters/ui :settings]
  ])

(comment
  [@(rf/subscribe [::subs/debug [:plugins]])]
  [@(rf/subscribe [::subs/plugin-keys])]
  )
