(ns midas.router.core)


;; rn I want the router to do specific api calls, and then route everything else to serving the same index response
;;
;;
(def routes
  [["/api"
    ["/read-line-items" {:name :api/read-line-items
                         :req-method :GET}]
    ["/add-line-item" {:name :api/add-line-item
                       :req-method :POST}]
    ["/read-tags" {:name :api/read-tags
                   :req-method :GET}]]
   ["/" :pages/home]
   ["/feed" :pages/feed]
   ["/data-input" :pages/data-input]])