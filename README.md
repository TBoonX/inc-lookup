
# Inc-Lookup

- resource URI is configured to be unique key
- different datasets can be added sequentially, reindexing of single datasets is problemantic however
- currently indexes labels and keeps track of the incoming links to a resource.

## Example Result (WIP)

```{
  "responseHeader":{
    "status":0,
    "QTime":1,
    "params":{
      "q":"label:\"Unis\"",
      "sort":"refCount desc",
      "_":"1542193469232"}},
  "response":{"numFound":2,"start":0,"docs":[
      {
        "resource":"http://dbpedia.org/resource/Georgia_(U.S._state)",
        "refCount":[4546],
        "label":["Georgia (Stati Uniti d'America)",
          "Geórgia (Estados Unidos)",
          "ジョージア州",
          "喬治亞州",
          "Georgia (U.S. state)",
          "Georgia",
          "Georgia (staat)",
          "Georgia",
          "Géorgie (États-Unis)",
          "جورجيا (ولاية أمريكية)",
          "Джорджия",
          "Georgia (Estados Unidos)"],
        "_version_":1617108133673435136},
      {
        "resource":"http://dbpedia.org/resource/United_States_Minor_Outlying_Islands",
        "label":["Kleine afgelegen eilanden van de Verenigde Staten",
          "United States Minor Outlying Islands",
          "美国本土外小岛屿",
          "جزر الولايات المتحدة الصغيرة النائية",
          "合衆国領有小離島",
          "United States Minor Outlying Islands",
          "Dalekie Wyspy Mniejsze Stanów Zjednoczonych",
          "Islas Ultramarinas Menores de Estados Unidos",
          "Ilhas Menores Distantes dos Estados Unidos",
          "Внешние малые острова США",
          "United States Minor Outlying Islands",
          "Isole minori esterne degli Stati Uniti d'America",
          "Îles mineures éloignées des États-Unis"],
        "_version_":1617108133668192256}]
  }}
  ```
  
  - Label field is multi-valued, allowing different data providers to point towards the same resource
  - RefCount field is increased while parsing through the different data sets
  
  ## Todo:
  
  - Approach generally allows multithreading, different datasets can be handled by different cores
  - Label duplicates could be removed. However: Duplicate labels might increase stability of highly frequented resources.
  
