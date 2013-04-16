db['iati-datasources'].update({"url": "http://projects.dfid.gov.uk/iati/Country/BD"}, { "$set" : { "url": "https://dl.dropboxusercontent.com/u/10717892/DFID-Bangladesh-BD-withlocations.xml"} })


// GET COUNTRY PROJECTS
START n=node:entities(type="iati-activity")
MATCH n-[:`recipient-country`]-a,
      n-[:`related-activity`]-p
WHERE n.hierarchy=2
AND   p.type=1
RETURN DISTINCT(p.ref)

// GET REGIONAL PROJECTS
START n=node:entities(type="iati-activity")
MATCH n-[r?:`recipient-region`]-a,
      n-[:`related-activity`]-p
WHERE n.hierarchy=2
// Parent Activity must have a 
AND   p.type=1
AND   (
      (r is not null)
  OR  (
        (r is null)
    AND (
         n.`recipient-region`! = "Balkan Regional (BL)"
      OR n.`recipient-region`! = "East Africa (EA)"
      OR n.`recipient-region`! = "Indian Ocean Asia Regional (IB)"
      OR n.`recipient-region`! = "Latin America Regional (LE)"
      OR n.`recipient-region`! = "East African Community (EB)"
      OR n.`recipient-region`! = "EECAD Regional (EF)"
      OR n.`recipient-region`! = "East Europe Regional (ED)"
      OR n.`recipient-region`! = "Francophone Africa (FA)"
      OR n.`recipient-region`! = "Central Africa Regional (CP)"
      OR n.`recipient-region`! = "Overseas Territories (OT)"
      OR n.`recipient-region`! = "South East Asia (SQ)"
    )
  )
)
RETURN DISTINCT(p.ref)

// GET GLOBAL PROJECTS
START n=node:entities(type="iati-activity")
MATCH n-[:`related-activity`]-p
WHERE n.hierarchy=2
AND  (n.`recipient-region`! = "Administrative/Capital (AC)" 
   OR n.`recipient-region`! = "Non Specific Country (NS)" 
   OR n.`recipient-region`! = "Multilateral Organisation (ZZ)")
AND   p.type=1
RETURN DISTINCT(p.ref) as id

START n=node:entities(type="iati-activity")
MATCH n-[:`recipient-country`]-c,
      n-[:sector]-s
WHERE n.hierarchy=2
AND   c.code="ET"
RETURN s.code as sector, s.sector as name, COUNT(s) as total
ORDER BY total DESC


START     n=node:entities(type="iati-activity")
MATCH     n-[:`budget`]-b-[:`value`]-v,
          n-[:`sector`]-s
WHERE     n.`iati-activity` = 'GB-CHC-1064413-GPAF-INN-001-DIFD2'
RETURN    n.`iati-activity` as id, s.code as code, s.sector as name,
          COALESCE(s.percentage?, 100) as percentage, sum(v.value) as val,
          (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total
ORDER BY  id ASC, total DESC

START  b=node:entities(type="budget")
MATCH  v-[:value]-b-[:budget]-n
WHERE  n.`iati-identifier` = 'GB-CHC-1064413-GPAF-INN-001-DIFD2'
RETURN v.value        as value, 
       v.`value-date` as date

START  n=node:entities(type="iati-activity")
MATCH  s-[:sector]-n-[:`budget`]-b-[:`value`]-v
WHERE  n.`iati-identifier` = 'GB-CHC-1064413-GPAF-INN-001-DIFD2'
RETURN s.code as code,
       s.sector? as name,
       (COALESCE(s.percentage?, 100) / 100.0 * sum(v.value)) as total


START  b=node:entities(type="budget")
MATCH  v-[:value]-b-[:budget]-n
WHERE  n.`iati-identifier` = 'GB-4-91071'
RETURN v.value        as value,
       v.`value-date` as date

START  activity = node:entities(type="iati-activity")
MATCH  status-[:`activity-status`]-activity-[:`reporting-org`]-org,
       activity-[?:title]-title,
       activity-[?:description]-description
WHERE  HAS(org.ref) AND org.ref IN ['GB-4']
RETURN COALESCE(activity.title?, title.title)                   AS title,
       COALESCE(activity.description?, description.description) AS description,
       activity.`iati-identifier`                               AS id,
       status.code                                              AS status

