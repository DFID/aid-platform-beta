
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

