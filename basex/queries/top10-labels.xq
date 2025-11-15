(: 1. Compter le nombre d'images qui contiennent la localisation 'loc right' :)
(: Note: On utilise distinct-values pour éviter de compter plusieurs fois la même image :)

<nb-images-loc-right>{
  count(
    distinct-values(
      /Images/image[
        Localizations/Localization[. = 'loc right']
        or LabelsLocalizationsBySentence/Sentence/Localization[. = 'loc right']
      ]/@Identifiant
    )
  )
}</nb-images-loc-right>



(: 2. Top 10 des labels les plus fréquents :)

let $labels :=
  /Images/image/(Labels/Label | LabelsLocalizationsBySentence/Sentence/Label)

let $normalized :=
  for $l in $labels
  let $norm := lower-case(normalize-space($l))
  where $norm ne '' and $norm ne 'none'
  return $norm

let $grouped :=
  for $val in distinct-values($normalized)
  let $count := count($normalized[. = $val])
  order by $count descending, $val
  return <label name="{$val}" count="{$count}"/>

return
  <labels-top10>
    { subsequence($grouped, 1, 10) }
  </labels-top10>
