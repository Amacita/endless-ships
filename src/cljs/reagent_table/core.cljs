;;; Forked from https://github.com/Frozenlock/reagent-table

(ns reagent-table.core
    (:require [re-frame.core :as rf]
              [reagent.core :as r]
              [reagent.dom :as rdom]
              [reagent.dom :as rdom]
              [reagent-table.utils :as utils]
              [endless-ships.subs :as subs]
              [endless-ships.views.utils :refer [kebabize]]
              [endless-ships.events :as events]))

(defn- recursive-merge
  "Recursively merge hash maps."
  [a b]
  (if (and (map? a) (map? b))
    (merge-with recursive-merge a b)
    b))

(defn- column-index-to-model
  "Convert the given column in view coordinates to
  model coordinates."
  [state-atom view-col]
  (-> @state-atom
      :col-index-to-model
      (nth view-col)))

(defn- is-sorting
  "Return the sort direction for the specified column number, nil
  if the column is not currently sorted, or :none if the column is not
  sortable at all. Column number must be in model coordinates."
  [sorting render-info model-col]
  (if (false? (:sortable render-info))
    :none
    (-> (filter #(= (first %) model-col)
                sorting)
        first
        second)))

(defn- header-cell-fn [render-info
                       view-col
                       model-col
                       config
                       state-atom
                       data-atom]
  (let [state         @state-atom
        col-hidden    (:col-hidden state)
        sort-fn       (:sort config)
        data-root-key (:data-root-key config)
        entity-type    (:entity-type config)
        column-model  (:column-model config)
        sortable      (not (false? (:sortable render-info)))
        sort-click-fn (fn [append]
                        (when (and sort-fn data-root-key)
                          (rf/dispatch [::events/toggle-ordering entity-type model-col append])))]
    [:th
     (recursive-merge
       (:th config)
       {:style (merge (get-in config [:th :style])
                      {:display (when (get col-hidden model-col) "none")})})
     [:span {:style {:padding-right 50}} (:header render-info)]
     (when (and sort-fn sortable)
       [:span {:style {:position "absolute"
                       :text-align "center"
                       :height "1.5em"
                       :width "1.5em"
                       :right "15px"
                       :cursor "pointer"}
               :on-click #(sort-click-fn (.-ctrlKey %))}
        (condp = @(rf/subscribe [::subs/sort-mode entity-type model-col])
          :asc " ▲"
          :desc " ▼"
          [:span {:style {:opacity "0.3"}}   ;; sortable but not participating
           " ▼"])])
     ]))

(defn- header-row-fn [column-model config data-atom state-atom]
  [:tr
   (doall (map-indexed (fn [view-col _]
                         (let [model-col (column-index-to-model state-atom view-col)
                               render-info (column-model model-col)]
                           ^{:key (or (:key render-info) model-col)}
                           [header-cell-fn render-info view-col model-col config state-atom data-atom]))
                       column-model))])

(defn- row-fn [row row-num row-key-fn state-atom config]
  (let [state @state-atom
        col-hidden (:col-hidden state)
        col-key-fn (:col-key config (fn [row row-num col-num] col-num))
        col-model  (:column-model config)
        cell-fn    (:render-cell config)]
    ^{:key (row-key-fn row row-num)}
    [:tr
     (doall
       (map-indexed (fn [view-col _]
                      (let [model-col (column-index-to-model state-atom view-col)]
                        ^{:key (col-key-fn row row-num model-col)}
                        [:td
                         {:style {:display (when (get col-hidden model-col) "none")}}
                         (cell-fn (col-model model-col) row row-num model-col)]))
                    (or
                      col-model
                      row)))]))

(defn- rows-fn [rows state-atom config]
  (let [row-key-fn (:row-key config (fn [row row-num] row-num))]
  (doall (map-indexed
           (fn [row-num row]
             (row-fn row row-num row-key-fn state-atom config))
           rows))))

(defn- init-column-index
  "Set up in the initial column-index-to-model numbers"
  [headers]
  (into [] (map-indexed (fn [idx _] idx) headers)))

(defn reagent-table
  "Create a table, rendering the vector held in data-atom and
  configured using the map config. The minimum requirements of
  config are :render-cell and :column-model.

  There is a distinction between view and model coordinates for
  column numbers. A column's view position may change if it is
  reordered, whereas its model position will be that of its index
  into :column-model

  :column-model is a vector of so-called render-info maps containing
   - :header A string for the header cell text
   - :key The reagent key for the column position in any rows. If
     absent defaults to the model index
   - :sortable false When :sort is present (see below) by default all
     columns are sortable. Otherwise any column can be excluded and no
     glyph will appear in its header.
  Other entries are as required by the client. The map is passed to
  the :render-cell function when cells are rendered.

  :render-cell a function that returns the hiccup for a table cell
    (fn [render-info row row-num col-num] (...))
  where render-info is the column entry, row is the vector child from
  data-atom, row-num is the row number and col-num is the column number
  in model coordiates.

  :table-state an atom used to hold table internal state. If supplied by
  the client then a way to see table state at the repl, and to allow the
  client to modify column order and sorting state.

  :row-key a function that returns a value to be used as the regaent key
  for rows
    (fn [row row-num] (...))
  where row is the vector child from data-atom, row-num is the row number.

  :sort a function to sort data-atom when a header cell sort arrow is clicked.
  Returns the newly sorted vector. If absent, the table is not sortable and no
  glyphs appear in the header.
    (fn [rows column-model sorting] (...))
  where rows is the vector to sort, column-model is the :column-model and sorting
  is a vector of vectors of the form [column-model-index :asc|:desc]. If the
  column-model entry includes :sortable false the individual column is excluded
  from sorting. Select multiple columns for sorting by using ctrl-click. Repeat
  to toggle the sort direction.

  :table the attributes applied to the [:table ... ] element. Defaults
  to {:style {:width nil}}}

  :thead the attributes applied to [:thead ...]

  :tbody the attributes applied to [:tbody ...]

  :caption an optional hiccup form for a caption
  "
  [data-atom config]
  (let [config (recursive-merge utils/default-table-config config)
        state-atom (or (:table-state config) (r/atom {})) ;; a place for the table local state
        {:keys [render-cell column-model]} config]
    (assert (and render-cell column-model)
            "Must provide :column-model and :render-cell in table config")
    (swap! state-atom assoc :col-index-to-model (init-column-index column-model))
    (fn []
      [:div
       ; Column selector
       [:div.column-selector
       (let [hidden-cols (r/cursor state-atom [:col-hidden])]
          (doall
            (map-indexed (fn [view-col _]
                           (let [model-col (column-index-to-model state-atom view-col)
                                 render-info (column-model model-col)
                                 hidden-a (r/cursor hidden-cols [model-col])
                                 checkbox-id (str "column-select-"
                                                  (name (:data-root-key config)) "-"
                                                  (name (:entity-type config)) "-"
                                                  (name (or (:key render-info) model-col)))]
                             ^{:key checkbox-id}
                             [:div.form-check
                              [:input.form-check-input
                               {:type "checkbox"
                                :value ""
                                :id checkbox-id
                                :checked (not @hidden-a)
                                :on-change #(do (swap! hidden-a not) nil)}]
                              [:label.form-check-label
                               {:for checkbox-id}
                               (:header render-info)]]))
                         column-model)))]
       ; Table
       [(let [table-container (:table-container config)]
          (r/create-class
            { :reagent-render (fn [] [:div.reagent-table-container
                                      table-container
                                      [:table.reagent-table (:table config)
                                       (when-let [caption (:caption config)]
                                         caption)
                                       [:thead (:thead config)
                                        (header-row-fn column-model
                                                       config
                                                       data-atom
                                                       state-atom)]
                                       [:tbody (:tbody config)
                                        (rows-fn @data-atom state-atom config)]]])}))]])))

(comment
  [@(rf/subscribe [::subs/sort-mode :ships 4])]
  [@(rf/subscribe [::subs/sort-mode :thrusters 4])]
  [@(rf/subscribe [::subs/entity-ordering :thrusters])]
  )
