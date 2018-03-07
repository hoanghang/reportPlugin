var IncidentsReport = IncidentsReport || {};

IncidentsReport.clickExport = function(){
    console.log("abc........");
}

IncidentsReport.validation = function () {
    var check = true;
        var fromDate = AJS.$("#txtStartDate").val();
        var toDate = AJS.$("#txtEndDate").val();
        if (fromDate == undefined || fromDate == null || fromDate == "") {
            AJS.$("#fromdate-validate").html('');
                AJS
                    .$("#fromdate-validate")
                    .append(
                        'Please enter a valid date!');
                check = false;
        }else{
            if (!moment(fromDate, "D/MMM/YY", true).isValid()) {
                AJS.$("#fromdate-validate").html('');
                AJS
                    .$("#fromdate-validate")
                    .append(
                        'Please enter a valid date: D/MMM/YY!');
                check = false;
            }
        }
        if (toDate == undefined || toDate == null || toDate == "") {
            AJS.$("#todate-validate").html('');
                AJS
                    .$("#todate-validate")
                    .append(
                        'Please enter a valid date!');
                check = false;
        }else{
            if (!moment(toDate, "D/MMM/YY", true).isValid()) {
                AJS.$("#todate-validate").html('');
                AJS
                    .$("#todate-validate")
                    .append(
                        'Please enter a valid date: D/MMM/YY!');
                check = false;
            }
        }

        if(toDate && fromDate){
            if(moment(toDate, "D/MMM/YY", true).isValid() && moment(fromDate, "D/MMM/YY", true).isValid()){
                var startDate = new Date(moment(fromDate,"D/MMM/YY").toDate().getTime());
                var endDate = new Date(moment(toDate,"D/MMM/YY").toDate().getTime());
                if(startDate> endDate){
                    AJS.$("#fromdate-validate").html('');
                    AJS.$("#fromdate-validate").append('From Date must less than To Date!');
                    check = false;
                }
            }
        }
        return check;
}

IncidentsReport.updateProjectsByCategories = function(){
    var projectCategory = AJS.$("#projectCat").val();

    var projectCategories = "";
    if (projectCategory == undefined || projectCategory == null || projectCategory == "") {
        projectCategories = "all";
    } else {
        for (var i = 0; i < projectCategory.length; i++) {
            if (i == 0) {
                projectCategories += projectCategory[i];
            } else {
                projectCategories += "," + projectCategory[i];
            }
        }
    }
    console.log("hang test");
    console.log(projectCategories);
    AJS.$.ajax({
        url: contextPath
        + "/rest/vnpt-report/1.0/report/search/getProjectsByProjectCategories/" + projectCategories,
        type: "GET",
        dataType: "json",
        success: function (data) {
            AJS.$("#projects").html('');
            var temp = "";
            for (var i = 0; i < data.length; i++) {
                temp += "<option value='" + data[i].key + "'>"
                    + data[i].name + "</option>";
            }
            AJS.$("#projects").auiSelect2("destroy");
            AJS.$("#projects").append(temp);
            AJS.$("#projects").auiSelect2();
        },
    });
}