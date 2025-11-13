(: Module RESTXQ pour BaseX – endpoints statistiques PadChest :)
module namespace api = "org.example.basex.api";

declare namespace rest = "http://exquery.org/rest";
declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

(: Helper: ajoute en-têtes CORS :)
declare %private function api:add-cors($nodes as node()*) as element()* {
  (
    <rest:header name="Access-Control-Allow-Origin" value="*"/>,
    <rest:header name="Access-Control-Allow-Methods" value="GET, OPTIONS"/>,
    <rest:header name="Access-Control-Allow-Headers" value="Authorization, Content-Type"/>
  )
};

(: Préflight OPTIONS :)
declare
  %rest:OPTIONS("/loc-right")
  %rest:OPTIONS("/top10-labels")
function api:options() {
  <empty/> (: renvoie 200 OK avec headers CORS :)
};

(: Endpoint: nombre d'images avec localisation 'loc right' :)
declare
  %rest:GET("/loc-right")
  %rest:produces("application/xml")
function api:loc-right() as element(nb) {
  let $count := count(collection("padchestDB")/Images/image[Localizations/Localization[lower-case(.) = 'loc right']])
  return <nb>{$count}</nb>
};

(: Endpoint: top 10 labels :)
declare
  %rest:GET("/top10-labels")
  %rest:produces("application/xml")
function api:top10-labels() as element(top10) {
  let $labels := collection("padchestDB")/Images/image/(Labels/Label | LabelsLocalizationsBySentence/Sentence/Label)
  let $norm := for $l in $labels
               let $n := lower-case(normalize-space($l))
               where $n != '' and $n != 'none'
               return $n
  let $group := for $v in distinct-values($norm)
                let $c := count($norm[. = $v])
                order by $c descending
                return <label name="{$v}" count="{$c}"/>
  return <top10>{subsequence($group, 1, 10)}</top10>
};

