
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
  
  - Solr eDisMax query parser works well on multiple labels (google-like search)
  
  ```
  {
  "responseHeader":{
    "status":0,
    "QTime":0,
    "params":{
      "mm":"50%",
      "q":"Billy~",
      "defType":"edismax",
      "qf":"label",
      "_":"1542193469232"}},
  "response":{"numFound":12,"start":0,"docs":[
      {
        "resource":"http://dbpedia.org/resource/Billy_Joel",
        "refCount":[311],
        "label":["Billy Joel",
          "Billy Joel",
          "بيلي جويل",
          "Billy Joel",
          "Billy Joel",
          "Billy Joel",
          "Billy Joel",
          "Billy Joel",
          "ビリー・ジョエル",
          "Billy Joel",
          "Джоэл, Билли",
          "Billy Joel"],
        "_version_":1617110813723590656},
      {
        "resource":"http://dbpedia.org/resource/Billy_Wilder",
        "refCount":[100],
        "label":["Billy Wilder",
          "Billy Wilder",
          "Billy Wilder",
          "Billy Wilder",
          "بيلي وايلدر",
          "Billy Wilder",
          "ビリー・ワイルダー",
          "Уайлдер, Билли",
          "Billy Wilder",
          "Billy Wilder",
          "Billy Wilder",
          "Billy Wilder"],
        "_version_":1617110813726736386},
      {
        "resource":"http://dbpedia.org/resource/Bill_Hicks",
        "refCount":[57],
        "label":["Bill Hicks",
          "Bill Hicks",
          "Bill Hicks",
          "Bill Hicks",
          "Bill Hicks",
          "بيل هيكس",
          "Bill Hicks",
          "Bill Hicks",
          "ビル・ヒックス",
          "Хикс, Билл",
          "Bill Hicks",
          "Bill Hicks"],
        "_version_":1617110813711007746},
      {
        "resource":"http://dbpedia.org/resource/Bill_Viola",
        "refCount":[2],
        "label":["Bill Viola",
          "Bill Viola",
          "Bill Viola",
          "Bill Viola",
          "Bill Viola",
          "Bill Viola",
          "Bill Viola",
          "Bill Viola",
          "ビル・ヴィオラ",
          "بيل فيولا",
          "Виола, Билл",
          "Bill Viola"],
        "_version_":1617110813715202049},
      {
        "resource":"http://dbpedia.org/resource/Jean-Claude_Killy",
        "label":["Килли, Жан-Клод",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "Jean-Claude Killy",
          "جان كلود كيلي",
          "ジャン＝クロード・キリー",
          "Jean-Claude Killy"],
        "_version_":1617110815373000704},
      {
        "resource":"http://dbpedia.org/resource/G.I._Bill",
        "label":["G.I. Bill of Rights",
          "G.I. Bill",
          "G.I. Bill",
          "G.I. Bill",
          "G. I. Bill"],
        "_version_":1617110814761680896},
      {
        "resource":"http://dbpedia.org/resource/Bill_Haley_&_His_Comets",
        "refCount":[44],
        "label":["Bill Haley & His Comets",
          "Bill Haley & His Comets",
          "Bill Haley & His Comets",
          "Bill Haley & His Comets",
          "Bill Haley & His Comets"],
        "_version_":1617110813706813440},
      {
        "resource":"http://dbpedia.org/resource/Carl_Bildt",
        "label":["Carl Bildt",
          "Carl Bildt",
          "Carl Bildt",
          "Carl Bildt",
          "Carl Bildt",
          "カール・ビルト",
          "Carl Bildt (1949)",
          "Carl Bildt",
          "Бильдт, Карл",
          "Carl Bildt",
          "Carl Bildt"],
        "_version_":1617110813890314240},
      {
        "resource":"http://dbpedia.org/resource/Billie_Holiday",
        "refCount":[127],
        "label":["بيلي هوليدي",
          "Billie Holiday",
          "ビリー・ホリデイ",
          "Billie Holiday",
          "Billie Holiday",
          "Билли Холидей",
          "Billie Holiday",
          "Billie Holiday",
          "Billie Holiday",
          "Billie Holiday",
          "Billie Holiday",
          "Billie Holiday"],
        "_version_":1617110813719396353},
      {
        "resource":"http://dbpedia.org/resource/Jelly_Roll_Morton",
        "label":["ジェリー・ロール・モートン",
          "Jelly Roll Morton",
          "Jelly Roll Morton",
          "Мортон, Джелли Ролл",
          "Jelly Roll Morton",
          "Jelly Roll Morton",
          "Jelly Roll Morton",
          "Jelly Roll Morton",
          "Jelly Roll Morton",
          "Jelly Roll Morton",
          "جيلي رول مورتون",
          "Jelly Roll Morton"],
        "_version_":1617110815406555136}]
  }}
  ```
  ## Todo:
  
  - Approach generally allows multithreading, different datasets can be handled by different cores
  - Label duplicates could be removed. However: Duplicate labels might increase stability of highly frequented resources.
  
  
  
