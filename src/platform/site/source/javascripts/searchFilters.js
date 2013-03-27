$(document).ready(function(){

getHiddenFieldsValues();
sortFilters();
addCheckboxesFilters();
setOnChange();
budgetFilteringSetUp();
setSectorGroupFilterClickable();

});

function hasTrue(array){
    if($.inArray(true, array)!= -1){
        return true;
    } else {
        return false;
    }
}

function budgetFilteringSetUp() {
var divsToCheck = $('input[name=status][type="hidden"]').parent('div');
var max = 0;
var min = 0;
$( "input[name=budget][type='hidden']" ).each(function(i, input){
    if(max < input.value){
      max = +(input.value);
    } 
});
$( "#slider-vertical" ).slider({
orientation: "horizontal",
range: true,
min: min,
max: max,
step : (Math.round(max / 100) * 100)/100,
values: [min,max],
slide: function( event, ui ) {
$( "#amount" ).html( ("£"+ui.values[0]).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,")+" - "+ ("£"+ui.values[1]).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,"));
},
change: function( event, ui ) {
  if(!$('input[type=checkbox]').is(':checked')){
    $(".search-result").each(function(i, div){
       if($(this).children("input[name='budget']").val() <= ui.values[1] && $(this).children("input[name='budget']").val() >= ui.values[0]){
        $(this).show();
        } else {
        $(this).hide();
        }
    });
  }else{
    filter(divsToCheck);
  }
  displayResultsAmount();
  }
});
$( "#amount" ).html( ("£"+min).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,") +" - "+("£"+max).replace(/(\d)(?=(?:\d{3})+$)/g, "$1,"));
}




function filter(divsToCheck){

if(!$('input[type=checkbox]').is(':checked')&&($('#slider-vertical').slider("option", "values")[1] == $('#slider-vertical').slider("option", "max"))) {
    $('.search-result').css("display", "inline");
    displayResultsAmount();
    return false;
}

divsToCheck.each(function(i, div){

            var anythingFound = false;
            var hasStatus = new Array();
            var isStatusGroupActive = false;
            var budget = 0;
            $('input:checked[name=status]').each(function(i, checkboxess){
                anythingFound = true;
                isStatusGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="status"]').length > 0){
                    hasStatus.push(true);
                }
             });
             
            var hasOrganisations = new Array();    
            var isOrganisationsGroupActive = false;
            $('input:checked[name=organizations]').each(function(i, checkboxess){
                anythingFound = true;
                isOrganisationsGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="organizations"]').length > 0){
                    hasOrganisations.push(true);
                }
             });

             var hasSectors = new Array();
             var isSectorsGroupActive = false;
              $('input:checked[name=sectors]').each(function(i, checkboxess){
                anythingFound = true;
                isSectorsGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="sectors"]').length > 0){
                    hasSectors.push(true);
                }
              });
             
              var hasCountries = new Array(); 
              var isCountriesGroupActive = false;
              $('input:checked[name=countries]').each(function(i, checkboxess){
                anythingFound = true;
                isCountriesGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="countries"]').length > 0){
                    hasCountries.push(true);
                }
              });
             
             var hasRegions = new Array(); 
             var isRegionsGroupActive = false;
              $('input:checked[name=regions]').each(function(i, checkboxess){
                anythingFound = true;
                isRegionsGroupActive = true;
                if($(div).children('input[value*="'+checkboxess.value+'"][name="regions"]').length > 0){
                    hasRegions.push(true);
                }
              });
             
            var show = new Array();
            if(isStatusGroupActive){
                show.push(hasTrue(hasStatus));
            } 
            if(isOrganisationsGroupActive){
                show.push(hasTrue(hasOrganisations));
            }
            if(isSectorsGroupActive){
                 show.push(hasTrue(hasSectors));
            }
            if(isCountriesGroupActive){
                 show.push(hasTrue(hasCountries));
            } 
            if(isRegionsGroupActive){
                show.push(hasTrue(hasRegions));
            }

            var divBudget = +($(div).children('input[name="budget"]').val());
            var min = +($('#slider-vertical').slider("option", "values")[0]);
            var max = +($('#slider-vertical').slider("option", "values")[1]);
            if(divBudget > max || divBudget < min) {
                show.push(false);
            } 

            if($.inArray(false, show)!= -1){
                $(div).css("display", "none");
                } else {
                $(div).css("display", "inline");
                }

            if(!anythingFound){
                $('.search-result').css("display", "none"); //show none cuz nothing found
            }
        });
displayResultsAmount();
}

function setOnChange(){
  var divsToCheck = $('input[name=status][type="hidden"]').parent('div');
  $('input[type=checkbox]').change(function() {
    filter(divsToCheck);
  });
}
 

function displayResultsAmount(){
  $('span[name=afterFilteringAmount]').html(($(".search-result").length - $(".search-result:hidden").length) + " of ");
}

var Status = new Array();
var Countries = new Array();
var Regions = new Array();
var Organizations = new Array();
var Sectors = new Array();
var SectorGroups = new Array();

function unique(array){
    return $.grep(array,function(el,index){
        return index == $.inArray(el,array);
    });
}

function SortByName(a, b){
  var aName = a.toLowerCase();
  var bName = b.toLowerCase(); 
  return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
}

function sortFilters(){
     Status = Status.sort(SortByName);
     Sectors = Sectors.sort(SortByName);
     Countries = Countries.sort(SortByName);
     Regions = Regions.sort(SortByName);
     Organizations = Organizations.sort(SortByName);
}

function splitAndAssign(string, outputArray){
    var splited = string.split('#')
      $.each(splited, function(i,val){
          if( val.length > 0){ 
            if($.inArray(val, outputArray)==-1){
                outputArray.push(val); 
            }
          }
      });
}

function getSectorGroup(sectorGroupId){
    if(sectorGroupId == "111") {return "Education, Level Unspecified";}
    else if(sectorGroupId == "112") { return "Basic Education";}
    else if(sectorGroupId == "113") { return "Secondary Education";}
    else if(sectorGroupId == "114") { return "Post-secondary Education";}
    else if(sectorGroupId == "121") { return "Health, General";}
    else if(sectorGroupId == "122") { return "Basic Health";}
    else if(sectorGroupId == "130") { return "Population Policies/programmes And Reproductive Health";}
    else if(sectorGroupId == "140") { return "Water And Sanitation";}
    else if(sectorGroupId == "151") { return "Government And Civil Society, General";}
    else if(sectorGroupId == "152") { return "Conflict Prevention And Resolution, Peace And Security";}
    else if(sectorGroupId == "160") { return "Other Social Infrastructure And Services";}
    else if(sectorGroupId == "210") { return "Transport And Storage";}
    else if(sectorGroupId == "220") { return "Communication";}
    else if(sectorGroupId == "230") { return "Energy Generation And Supply";}
    else if(sectorGroupId == "240") { return "Banking And Financial Services";}
    else if(sectorGroupId == "250") { return "Business And Other Services";}
    else if(sectorGroupId == "311") { return "Agriculture";}
    else if(sectorGroupId == "312") { return "Forestry";}
    else if(sectorGroupId == "313") { return "Fishing";}
    else if(sectorGroupId == "321") { return "Industry";}
    else if(sectorGroupId == "322") { return "Mineral Resources And Mining";}
    else if(sectorGroupId == "323") { return "Construction";}
    else if(sectorGroupId == "331") { return "Trade Policy And Regulations And Trade-related Adjustment";}
    else if(sectorGroupId == "332") { return "Tourism";}
    else if(sectorGroupId == "410") { return "General Environmental Protection";}
    else if(sectorGroupId == "430") { return "Other Multisector";}
    else if(sectorGroupId == "510") { return "General Budget Support";}
    else if(sectorGroupId == "520") { return "Developmental Food Aid/food Security Assistance";}
    else if(sectorGroupId == "530") { return "Other Commodity Assistance";}
    else if(sectorGroupId == "600") { return "Action Relating To Debt";}
    else if(sectorGroupId == "720") { return "Emergency Response";}
    else if(sectorGroupId == "730") { return "Reconstruction Relief And Rehabilitation";}
    else if(sectorGroupId == "740") { return "Disaster Prevention And Preparedness";}
    else if(sectorGroupId == "910") { return "Administrative Costs Of Donors";}
    else if(sectorGroupId == "920") { return "Support To Non-Governmental Organisations (NGOS)";}
    else if(sectorGroupId == "930") { return "Refugees In Donor Countries";}
    else if(sectorGroupId == "998") { return "Unallocated/Unspecified";}
    else { return "Ungrouped"}
}

function getGroupFromCode(code){
  return code.substring(0,3)
}

function getHiddenFieldsValues(){
    $(".search-result input[name=status]").each(function() {
    splitAndAssign($(this).attr("value"),Status)
    });

    $(".search-result input[name=organizations]").each(function() {
    splitAndAssign($(this).attr("value"),Organizations)
    });

    $(".search-result input[name=countries]").each(function() {
    splitAndAssign($(this).attr("value"),Countries)
    });

    $(".search-result input[name=sectors]").each(function() {
    splitAndAssign($(this).attr("value"),Sectors)
    });

    $(".search-result input[name=regions]").each(function() {
    splitAndAssign($(this).attr("value"),Regions)
    });
}

function addCheckboxesFilters(){
    $.each( Status, function( key, value ) {
    $("div[name=status]").append("&nbsp;<input type='checkbox' name='status' value='"+value+"'>&nbsp;"+value+"</input><br>")
    });

    $.each( Sectors, function( key, value ) {
    var splited = value.split('@');
    var sectorGroupName = getSectorGroup(getGroupFromCode(splited[0]));
    if($("div[name=sectorGroup][group='"+sectorGroupName+"']").length > 0 ){
      $("div[name=sectorGroup][group='"+sectorGroupName+"']").append("&nbsp;<input type='checkbox' name='sectors'  value='"+splited[1]+"'><span >&nbsp;"+splited[1]+"</span></input><br>")
    }else{
      $("div[name=sectors]").append("&nbsp;<br><div name='sectorGroup' group='"+sectorGroupName+"'><span style='font-size: 1.1em;' name='sectorGroupClickable'>&nbsp;"+sectorGroupName+"</span><br></div>");
      $("div[name=sectorGroup][group='"+sectorGroupName+"']").append("&nbsp;<input type='checkbox' name='sectors'  value='"+splited[1]+"'><span >&nbsp;"+splited[1]+"</span></input><br>")
    }
    });
    $("div[name=sectorGroup]").children(':not(span[name=sectorGroupClickable])').hide();

    $.each( Countries, function( key, value ) {
    $("div[name=countries]").append("&nbsp;<input type='checkbox' name='countries' value='"+value+"'>&nbsp;"+value+"</input><br>");
    });

    $.each( Regions, function( key, value ) {
    $("div[name=regions]").append("&nbsp;<input type='checkbox' name='regions'  value='"+value+"'>&nbsp;"+value+"</input><br>");
    });

    $.each( Organizations, function( key, value ) {
    $("div[name=organizations]").append("&nbsp;<input type='checkbox' name='organizations'  value='"+value+"'>&nbsp;"+value+"</input><br>");
    });
}

function setSectorGroupFilterClickable(){
  $("span[name=sectorGroupClickable]").each(function(i, span){
    $(span).click(function(){
      if($(this).parent().children(':not(span[name=sectorGroupClickable])').is(":visible")){
        $(this).parent().children(':not(span[name=sectorGroupClickable])').hide();
      } else {
        $(this).parent().children(':not(span[name=sectorGroupClickable])').show();
      }
    });
  });
}