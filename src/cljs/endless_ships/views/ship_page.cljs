(ns endless-ships.views.ship-page
  (:require [re-frame.core :as rf]
            [endless-ships.subs :as subs]
            [endless-ships.views.utils :refer [render-attribute render-percentage
                                               render-description nbspize kebabize]]
            [endless-ships.utils.ships :refer [total-cost or-zero]]
            [endless-ships.routes :as routes]))

(defn- render-licenses [[license1 license2]]
  (if (some? license2)
    [:p.italic (str "This ship requires " license1 " and " license2 " licenses.")]
    [:p.italic (str "This ship requires a " license1 " license.")]))

(defn- image-url [ship]
  (let [filename (get-in ship [:meta :image :file])
        plugin-dir (:base-image-url @(rf/subscribe [::subs/plugin (get-in ship [:meta :image :origin])]))]
    (js/window.encodeURI (str plugin-dir filename))))

(defn ship-modifications [ship-name selected-modification-slug modification-names]
  [:div.card.text-dark.bg-light
   [:h3.card-header "Modifications"]
   [:div.card-body
    [:div.list-group
     [:a.list-group-item.list-group-item-action
      {:class (when (nil? selected-modification-slug) "active")
       :aria-current (when (nil? selected-modification-slug) "true")
       :href (routes/ship-url ship-name)}
      (nbspize ship-name)]
     (for [modification-name modification-names]
       ^{:key modification-name}
       [:a.list-group-item.list-group-item-action
        {:class (when (= (kebabize modification-name) selected-modification-slug) "active")
        :aria-current (when (= (kebabize modification-name) selected-modification-slug) "true")
        :href (routes/ship-modification-url ship-name modification-name)}
       (nbspize modification-name)])]]])

(def outfit-categories
  ["Guns"
   "Turrets"
   "Secondary Weapons"
   "Ammunition"
   "Systems"
   "Power"
   "Engines"
   "Hand to Hand"
   "Special"])

(defn outfits-list [outfits]
  [:div.card.text-dark.bg-light
   [:h3.card-header "Default Outfits"]
    (->> outfit-categories
         (map (fn [category]
                (when (contains? outfits category)
                  ^{:key category}
                  [:div.card-body
                   [:div.card-title category]
                   [:ul.list-group
                    (->> (get outfits category)
                         (sort-by #(get-in % [:outfit :name]))
                         (map (fn [{:keys [outfit quantity]}]
                                ^{:key (:name outfit)}
                                (let [link (routes/outfit-link (:name outfit))]
                                  (if (= quantity 1)
                                    [:li.list-group-item link]
                                    [:li
                                     {:class [:list-group-item
                                              :d-flex
                                              :justify-content-between
                                              :align-items-center]}
                                     link [:span.badge.bg-secondary.rounded-pill quantity]])))))]]))))])

(defn ship-page [ship-name ship-modification]
  (let [ship @(rf/subscribe [::subs/ship ship-name])
        all-outfit-list @(rf/subscribe [::subs/outfits])
        outfits (zipmap (map :name all-outfit-list) all-outfit-list)
        modification-names @(rf/subscribe [::subs/ship-modifications-names ship-name])
        selected-modification (if (some? ship-modification)
                                @(rf/subscribe [::subs/ship-modification ship-name ship-modification])
                                {})
        ship-with-modification (merge ship selected-modification)
        ship-outfits (->> (:outfits ship-with-modification)
                          (map (fn [{:keys [name quantity]}]
                                 (let [outfit (get outfits name)]
                                   {:outfit outfit
                                    :quantity quantity})))
                          (group-by #(get-in % [:outfit :category])))]
    [:div.app
     [:div.row.mb-3
      [:div.col-md-6
       [:div.card.text-dark.bg-light.mb-3
        [:h3.card-header (:name ship)]
        [:div.card-body.clearfix.pr-3
        [:img.ship-sprite.float-end.mb-3.ms-md-3
         {:src (image-url ship-with-modification)}]
           [:ul
            (render-attribute ship-with-modification total-cost "cost")
            (render-attribute ship-with-modification :shields "shields")
            (render-attribute ship-with-modification :hull "hull")
            (render-attribute ship-with-modification :mass "mass")
            (render-attribute ship-with-modification :cargo-space "cargo space")
            (render-attribute ship-with-modification :required-crew "required crew")
            (render-attribute ship-with-modification :bunks "bunks")
            (render-attribute ship-with-modification :fuel-capacity "fuel capacity")
            (render-attribute ship-with-modification :outfit-space "outfit space")
            (render-attribute ship-with-modification :weapon-capacity "weapon capacity")
            (render-attribute ship-with-modification :engine-capacity "engine capacity")
            (render-attribute ship-with-modification (or-zero :guns) "guns")
            (render-attribute ship-with-modification (or-zero :turrets) "turrets")
            (when (pos? (:drones ship-with-modification))
              (render-attribute ship-with-modification :drones "drones"))
            (when (pos? (:fighters ship-with-modification))
              (render-attribute ship-with-modification :fighters "fighters"))
            (render-attribute ship-with-modification :ramscoop "ramscoop")
            (render-attribute ship-with-modification :cloak "cloak")
            (render-percentage ship-with-modification :self-destruct "self-destruct")]
           (when (some? (:licenses ship-with-modification))
             (render-licenses (:licenses ship-with-modification)))]]
       (when (seq modification-names)
         (ship-modifications (:name ship) ship-modification modification-names))]
      [:div.col-md-6
        [outfits-list ship-outfits]]]
     (when (seq (:description ship-with-modification))
       [:div.row
        [:div.col
         [:div.card.text-dark.bg-light
          [:div.card-body
           [:p.card-text (render-description ship-with-modification)]]]]])]))
