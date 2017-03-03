/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

window.everit = window.everit || {};
everit.jttp = everit.jttp || {};
everit.jttp.main = everit.jttp.main || {};

(function(jttp, jQuery) {
  
  jQuery(document).ready(function() {

    jQuery('.aui-ss-editing').attr("style", "width: 250px;");
    jQuery('.aui-ss.aui-ss-editing .aui-ss-field').attr("style", "width: 250px;");

    jttp.calculateDuration();
    durationSelectionSetup();
    issuePickerSetup();
    eventBinding();
    commentsCSSFormat();

    if(!jQuery( ".aui-message-error" ).length 
        && window.location.search.indexOf('date') > -1
        && !isContainsAchorExlucdingParts(window.location.search)){
      document.getElementById("issueSelect-textarea").focus();
      var anchorDiv = document.getElementById("buttons-container");
      jQuery(window).scrollTop( anchorDiv.offsetTop);
    }else{
      jQuery("#jttp-headline-day-calendar").blur();
    }
  
//  Set the correct jira server date to the date picker   
    jQuery("#dateHidden").val(jttp.options.jiraFormatedDate);

    //set end date 
    jQuery("#enddate").val(jttp.options.dateEndFormated);
    
    var calTo = Calendar.setup({
      firstDay : jttp.options.firstDay,
      inputField : jQuery("#enddate"),
      button : jQuery("#date_trigger_end"),
      date : jttp.options.dateEndFormated,
      ifFormat: jttp.options.dateFormat,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : jttp.options.useISO8601,
    });
    
    popupCalendarsSetup();
    setExcludeDaysToWeekend(jttp.options.excludeDays);
    setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    
    if (jttp.options.actionFlag == "editAll") {
      disableInputFields();
    }
    if(jttp.options.worklogSize == 0){
      AJS.messages.info({
        title: AJS.I18n.getText("plugin.no.worklogs.title"),
        body: AJS.I18n.getText("plugin.no.worklogs"),
        closeable: false,
      });
    }

    jttp.periodCheck();

    addTooltips();
    headlineProgressIndicator();
    initProgrssIndicators();
    initTooltipsForIndicators();
    var original = Calendar.prototype.show;
    Calendar.prototype.show = function() {
      original.call(this);
      setExcludeDaysToWeekend(jttp.options.excludeDays);
      setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    }
    if(location.href.indexOf('showWarning')>=0){
    	jQuery("#futorelog-warning").slideToggle("slow");
    }
    
    userWizardDialogInit();
  });
  
  function isContainsAchorExlucdingParts(search){
    var exlucdingParts = ["datesubmit", "dayBack", "dayNext","today", "actionFlag=delete","actionFlag=copy", "lw_chgdate" ];
    var contains = false;
    exlucdingParts.forEach(function(item){
      if(search.indexOf(item) != -1){
        contains = true;
      }
    });
    return contains;
  }
  
  function addTooltips(){
    var $issueTypeTooltip = jQuery('#jttp-worklog-issue-type');
    if(!$issueTypeTooltip.hasClass('jtrp-tooltipped')) {
      $issueTypeTooltip.tooltip({gravity: 'w'});
      $issueTypeTooltip.addClass('jtrp-tooltipped');
    }
    
    var $datePickerTooltip = jQuery('#jttp-headline-day-calendar');
    if(!$datePickerTooltip.hasClass('jtrp-tooltipped')) {
      $datePickerTooltip.tooltip();
      $datePickerTooltip.addClass('jtrp-tooltipped');
    }
    
    jQuery('.tooltip-left').each(function() {
      var $element = jQuery(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip({gravity: 'e'});
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    jQuery('.tooltip-bottom').each(function() {
      var $element = jQuery(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip();
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    jQuery('.img-tooltip').each(function() {
      var $element = jQuery(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip({gravity: 'w'});
        $element.addClass('jtrp-tooltipped');
      }
    });
  }
  
  
  jttp.startState = 0;
  jttp.endState = 0;

  jttp.dateChanged = function(calendar) {
    var dmy = AJS.Meta.get("date-dmy").toUpperCase();
    jQuery("#dateHidden").val(calendar.date.format(dmy));
    jQuery("#dateHidden").change();
  }

  jttp.startNowClick = function(startTimeChange) {
    if (jttp.startState == 0) {
      setStartNow();
    } else if (jttp.startState == 1) {
      setStartInc(startTimeChange);
    } else if (jttp.startState == 2) {
      setStartDecTemporary(startTimeChange);
    }
  }

  jttp.endTimeInputClick = function(addFocus) {
    if (jttp.options.actionFlag != "editAll") {
      var $input = jQuery("#endTimeInput").css("cursor", "text")
                     .hide()
                     .prev()
                     .prop("disabled", false)
                     .css("cursor", "text");
      if(addFocus) {
        $input.focus();
      }
      jQuery("#durationTimeInput").css("cursor", "pointer")
        .show()
        .prev("input")
        .prop("disabled", true)
        .css("cursor", "pointer");
      jQuery("#radioEnd").prop("checked", true);
    }
  }

  jttp.endNowClick = function(endTimeChange) {
    if (jttp.endState == 0) {
      setEndNow();
    } else if (jttp.endState == 1) {
      setEndInc(endTimeChange);
    } else if (jttp.endState == 2) {
      setEndDecTemporary(endTimeChange);
    }
  }

  jttp.durationTimeInput = function(addFocus) {
    if (jttp.options.actionFlag != "editAll") {
      var $input = jQuery("#durationTimeInput").css("cursor", "text")
                     .hide()
                     .prev("input[disabled]")
                     .prop("disabled", false)
                     .css("cursor", "text");
      if(addFocus){
        $input.focus();
      }
      jQuery("#endTimeInput").css("cursor", "pointer")
        .show()
        .prev("input")
        .prop("disabled", true)
        .css("cursor", "pointer");
      jQuery("#radioDuration").prop("checked", true);
    }
  }

  function timeZoneCorrection(date){
    var osTimeZoneOffset = date.getTimezoneOffset() * -60000;
    var correctMil = date.getTime() + osTimeZoneOffset;
    return correctMil;
  }
  
  var actualWizardPage;
  
  function userWizardDialogInit() {
    if(jttp.options.showUserWizard){
      AJS.dialog2("#user-wizard-dialog").show();
      actualWizardPage = 1
      changeNavigationButtonVisibility();
    }
  };
  
  jttp.prevWizardPage = function(){
    if(actualWizardPage > 1){
      actualWizardPage--;
    }else{
      actualWizardPage = 1;
    }
    changeNavigationButtonVisibility();
    showActiveTutotialPage();
   };

   jttp.nextWizardPage = function(){
     if(actualWizardPage < 6){
       actualWizardPage++;
     }else{
       actualWizardPage = 6;
      }
      changeNavigationButtonVisibility();
      showActiveTutotialPage();
   };
   
   function changeNavigationButtonVisibility(){
     if(actualWizardPage == 1){
       jQuery("#wizard-dialog-prev").hide();
     }else{
       jQuery("#wizard-dialog-prev").show();
     }
     if(actualWizardPage == 6){
       jQuery("#wizard-dialog-next").hide();
     }else{
       jQuery("#wizard-dialog-next").show();
     }
     if(actualWizardPage == 6){
       jQuery("#user-wizard-close").hide();
       jQuery("#user-wizard-save").show();
     }else{
       jQuery("#user-wizard-close").show();
       jQuery("#user-wizard-save").hide();
     }
   }
   
   function getUserSettingsValuesJson(){
     //text fields
     var startTime = jQuery('#uw_startTime').val() || "";
     var endTime = jQuery('#uw_endTime').val() || "";
     var defaultStartTime = jQuery('#uw_defaultStartTime').val() || "";
     //radio buttons
     var currentOrLast = jQuery('input[name="uw_currentOrLast"]:checked').val();
     var isActualDate  = false;
     if(currentOrLast == "current"){
       isActualDate = true;
     }
     var progressInd = jQuery('input[name="uw_progressInd"]:checked').val();
     var progressIndDaily  = false;
     if(progressInd == "daily"){
       progressIndDaily = true;
     }
     var activeField = jQuery('input[name="uw_activeField"]:checked').val();
     var activeFieldDuration  = false;
     if(activeField == "duration"){
       activeFieldDuration = true;
     }
     var isShowIssueSummaryChecked = jQuery('input[name="uw_isShowIssueSummary"]:checked').val();
     var isShowIssueSummary  = false;
     if(isShowIssueSummaryChecked == "showIssueSummary"){
       isShowIssueSummary = true;
     }
     //check buttons
     var isColoringChecked =  jQuery('input[name="uw_isColoring"]:checked').val();
     var isColoring = false
     if(isColoringChecked != null){
       isColoring = true;
     }
     var isShowFutureLogWarningChecked =  jQuery('input[name="uw_isShowFutureLogWarning"]:checked').val();
     var isShowFutureLogWarning = false
     if(isShowFutureLogWarningChecked != null){
       isShowFutureLogWarning = true;
     }
     var showRemaningEstimateChecked =  jQuery('input[name="uw_showRemaningEstimate"]:checked').val();
     var showRemaningEstimate = false
     if(showRemaningEstimateChecked != null){
       showRemaningEstimate = true;
     }
     var showPeriodWorklogsChecked =  jQuery('input[name="uw_showPeriodWorklogs"]:checked').val();
     var showPeriodWorklogs = false
     if(showPeriodWorklogsChecked != null){
       showPeriodWorklogs = true;
     }
     var showPeriodWorklogsChecked =  jQuery('input[name="uw_showPeriodWorklogs"]:checked').val();
     var showPeriodWorklogs = false
     if(showPeriodWorklogsChecked != null){
       showPeriodWorklogs = true;
     }
     var isRoundedChecked =  jQuery('input[name="uw_isRounded"]:checked').val();
     var isRounded = false
     if(isRoundedChecked != null){
       isRounded = true;
     }
     
     var userSettingsValues = {
       "activeFieldDuration": activeFieldDuration,
       "defaultStartTime": defaultStartTime,
       "endTime": endTime,
       "actualDate": isActualDate,
       "coloring": isColoring,
       "rounded": isRounded,
       "showFutureLogWarning": isShowFutureLogWarning,
       "showIssueSummary": isShowIssueSummary,
       "progressIndDaily": progressIndDaily,
       "showPeriodWorklogs": showPeriodWorklogs,
       "showRemaningEstimate": showRemaningEstimate,
       "startTime": startTime,
     }
     return userSettingsValues;
   }
   
   function showActiveTutotialPage(){
     jQuery("#user-wizard-dialog .tabs-pane.active-pane").removeClass("active-pane");
     jQuery("#user-wizard-dialog .aui-progress-tracker-step.aui-progress-tracker-step-current").removeClass("aui-progress-tracker-step-current");
     jQuery("#page-"+ actualWizardPage).addClass("active-pane");
     jQuery("#porgtrack-"+ actualWizardPage).addClass("aui-progress-tracker-step-current");
   };
  
   jttp.userWizardDialogHide = function(){
     var userSettingsValues = getUserSettingsValuesJson();
     var json = JSON.stringify(userSettingsValues);
     var userSettingsValuesJson = jQuery('#userSettingsValuesJson');
     userSettingsValuesJson.val(json);
     
    AJS.dialog2("#user-wizard-dialog").hide();
    return true;
   }
   
  jttp.periodCheck = function(){
    if(jttp.options.showPeriodWorklogs){
      if(jttp.options.actionFlag != "edit"){
        if (jQuery("#usePeriod").is(":checked")) {
          document.getElementById("enddate").disabled = false;
        } else {
          document.getElementById("enddate").disabled = true;
        }
      }else{
        if (jQuery("#usePeriod").is(":checked")) {
          document.getElementById("usePeriod").checked = false;
        }
        document.getElementById("usePeriod").disabled = true;
        document.getElementById("enddate").disabled = true;
      }
    }
  }
  
  jttp.beforeSubmit = function() {
    var date = jQuery('#date');
    //Send back the jira server time
    date.val(jttp.options.currentServerTime);
    var worklogValues = getWorklogValuesJson();
    if(worklogValues == false){
      jttpLogworkSaveButtonEnable();
      return false;
    }
    var json = JSON.stringify(worklogValues);
    var worklogValuesJson = jQuery('#worklogValuesJson');
    worklogValuesJson.val(json);
    
    // Added Piwik Submit action to save action. (count create, edit, edit all
	// saves)
    _paq.push(['trackEvent', 'User', 'Submit']);
    return true;
  }

  jttp.setActionFlag = function(flag, id) {
    var actionFlag = jQuery('.actionFlag_'+id);
    actionFlag.val(flag);
    actionFlag.change();
  }

  jttp.actionSubmitClick  = function(id) {
    jQuery("#actionSubmit_"+id).click();
  }

  jttp.beforeSubmitEditAll = function(){
    var date = jQuery('#date');
    date.val(jttp.options.currentServerTime);
    jQuery("#jttp-editall-form").append(date);

    return true;
  }

  jttp.beforeSubmitAction = function(id) {
    var date = jQuery('#date');
    date.val(jttp.options.currentServerTime);
    jQuery(".actionForm_"+id).append(date);

    return true;
  }

  jttp.cancelClick = function(){
    window.location = "JiraTimetrackerWebAction.jspa?date="+jttp.options.currentServerTime;
  }

  jttp.beforeSubmitChangeDate = function() {
    var dateHidden = jQuery('#dateHidden').val();
    var currentJiraTime;
    if(dateHidden!=jttp.options.jiraFormatedDate){
    	//the date changed by the calendar
    	//the correct jira time should be created
    	var dateInMil = Date.parseDate(dateHidden, jttp.options.dateFormat);
    	var offset=dateInMil.getTimezoneOffset()*60000;
    	var dateInMilisec=dateInMil.getTime()-offset;
    	dateInMilisec= dateInMilisec-jttp.options.userTimeZoneOffset+7200000;
    	currentJiraTime=dateInMilisec;
    }else {
    	//the date changed by the next or previous button
    	//add or subtract handled in server side
    	currentJiraTime=jttp.options.currentServerTime;
    }
    var date = jQuery('#date');
    date.val(currentJiraTime);
    jQuery("#jttp-datecahnge-form").append(date);
    
    var worklogValues = getWorklogValuesJson();
    if(worklogValues == false){
      jttpLogworkSaveButtonEnable();
      return false;
    }
    var json = JSON.stringify(worklogValues);
    var worklogValuesJson = jQuery('#worklogValuesJson');
    worklogValuesJson.val(json);
    jQuery("#jttp-datecahnge-form").append(worklogValuesJson);
    
    var actionWorklogId = jQuery('#jttp-logwork-form #actionWorklogId');
    jQuery("#jttp-datecahnge-form").append(actionWorklogId);
    
    var editAll = jQuery('#jttp-logwork-form #editAll');
    jQuery("#jttp-datecahnge-form").append(editAll);
    
    var isShowMoveAllNoPermission = jQuery('#jttp-logwork-form #isShowMoveAllNoPermission');
    jQuery("#jttp-datecahnge-form").append(isShowMoveAllNoPermission);
    
    var actionFlag = jQuery('#jttp-logwork-form #actionFlag');
    jQuery("#jttp-datecahnge-form").append(actionFlag);
    
    return true;
  }
  
  jttp.handleEnterKey = function(e, setFocusTo) {
    var isEnter = e.keyCode == 10 || e.keyCode == 13;
    if (!isEnter) {
      return;
    }
    if (e.ctrlKey) {
      jQuery('#jttp-logwork-save, #Edit').click();
    } else {
      e.preventDefault ? e.preventDefault() : event.returnValue = false;
      jQuery(setFocusTo).focus();
      return false;
    }
  }

  function disableInputFields() {
	jQuery("#dummyFormWrapper").hide();
	jQuery("#wokrlogChangeDateMessage").show();
    jQuery("#startTime").prop("disabled", true);
    jQuery("#startNow").prop("disabled", true);
    jQuery("#endTime").prop("disabled", true);
    jQuery("#endNow").prop("disabled", true);
    jQuery("#durationTime").prop("disabled", true);
    jQuery("#issueSelect-textarea").prop("disabled", true);
    jQuery("#issueSelect-textarea").prop("disabled", true);
    jQuery("#comments").prop("disabled", true);
  }
  
  function headlineProgressIndicator() {
    var jttp_progress_red = '#d04437';
    var jttp_progress_green = '#14892c';
    var jttp_progress_yellow = '#f6c342';
    var $indicator = jQuery('#jttp-headline-progress-indicator');
    var dailyPercent = parseFloat($indicator.attr('data-jttp-percent'));
    if(dailyPercent > 1.0) {
      dailyPercent = 1;
    }
    AJS.progressBars.update($indicator, dailyPercent);
    var $progressIndicator = jQuery('#jttp-headline-progress-indicator .aui-progress-indicator-value');
    if (dailyPercent <= 0.2) {
      jQuery($progressIndicator).css("background-color", jttp_progress_red);
    } else if (dailyPercent >= 1.0){
      jQuery($progressIndicator).css("background-color", jttp_progress_green); 
    } else {
      jQuery($progressIndicator).css("background-color", jttp_progress_yellow);  
    }
  }
  
  function initProgrssIndicators(){
		  jQuery('.progress').each(function(i, obj) {
		    var $obj = jQuery(obj);
			   var widthInprecent = parseFloat($obj.attr('data-jttp-percent'));
			   if(widthInprecent > 1.0) {
			     widthInprecent = 1;
			    }
				  if(widthInprecent < 0.2){
					  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
						  jQuery( obj ).addClass( "progress-bar-danger" );   
					   });
				  } else if(widthInprecent < 1){
					  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
						  jQuery( obj ).addClass( "progress-bar-warning" );   
					   });
				  }else {
					  jQuery( obj ).children('.progress-bar').each(function(i, obj) {
						  jQuery( obj ).addClass( "progress-bar-success" );  
					   });
				  }
		  });
  }
  function initTooltipsForIndicators(){
	  jQuery('.jttpTooltip').each(function(i, obj) {
		  jQuery(obj).tooltip({
		      title: function () {
		          return jQuery( obj ).children('.jttpTooltiptext').html();
		      },
		    html: true 
		  });  
		  });
  }
  function eventBinding() {
    jQuery('.table-endtime').click(function() {
      var temp = new String(jQuery(this).html());
      jQuery('#startTime').val(temp.trim());
    });

    jQuery('.table-starttime').click(function() {
      var temp = new String(jQuery(this).html());
      jQuery('#endTime').val(temp.trim());
    });

    jQuery('.table-comment').click(function() {
      var temp = jQuery(this).find('#hiddenWorklogBody').val();
      var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
      var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");
      jQuery("#comments").html(temp2);
      jQuery("#comments").val(temp3);
    });

    jQuery('.table-issue').click(function() {
      jQuery('#issueSelect-textarea').parent().find('.item-delete').click();

      var temp = new String(jQuery(this).find('a').attr("jttp-data-issue-key"));
      jQuery('#issueSelect-textarea').val(temp.trim());
      jQuery('#issueSelect-textarea').focus();
      jQuery('#Edit').focus();
      jQuery('#jttp-logwork-save').focus();
    });

    jQuery('.copy').click(function() {
      jQuery('#issueSelect-textarea').parent().find('.item-delete').click();
      var temp = jQuery(this).parent().parent().find('.table-issue').find('a').html();
      jQuery('#issueSelect-textarea').val(temp.trim());

      temp = jQuery(this).parent().parent().find('#hiddenWorklogBody').val();
      var temp2 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "&#013;");
      var temp3 = temp.replace(/(\\r\\n|\\n|\\r)/gm, "\n");

      jQuery("#comments").html(temp2);
      jQuery("#comments").val(temp3);

      jQuery('#issueSelect-textarea').focus();
      jQuery('#jttp-logwork-save').focus();
    });

    jQuery('#issueSelect-textarea').keydown(function(e) {
      var isEnter = e.keyCode == 10 || e.keyCode == 13;
      if (isEnter && e.ctrlKey) {
        jQuery('#jttp-logwork-save, #Edit').click();
      }
    });

    jQuery('#comments').keydown(function(e) {
      if ((e.keyCode == 10 || e.keyCode == 13) && e.ctrlKey) {
        jQuery('#jttp-logwork-save, #Edit').click();
      }
    });

    jQuery('#jttp-logwork-form').submit(function() {
      jQuery('#jttp-logwork-save').prop("disabled", true);
      jQuery('#lw_save').val('true');
      jQuery('#lw_save').attr('disabled', false);
      return true;
    });
  }

  function jttpLogworkSaveButtonEnable() {
    jQuery('#jttp-logwork-save').prop("disabled", false);
    jQuery('#lw_save').val('false');
    jQuery('#lw_save').attr('disabled', true);
  }

  function durationSelectionSetup() {
    if(jttp.options.defaultCommand){
      if(jttp.options.activeFieldDuration){
        jttp.durationTimeInput(false);
      } else {
        jttp.endTimeInputClick(false);
      }
    } else {
      if (jttp.options.isDurationSelected) {
        jttp.durationTimeInput(false);
      } else {
        jttp.endTimeInputClick(false);
      }
    }
  }

  function issuePickerSetup() {
    var ip = new AJS.IssuePicker({
      element : jQuery("#issueSelect"),
      userEnteredOptionsMsg : AJS.params.enterIssueKey,
      uppercaseUserEnteredOnSelect : true,
      singleSelectOnly : true,
      currentProjectId : jttp.options.projectsId,
    });

    var selectedArray = jQuery.makeArray( jttp.options.issueKey );
    jQuery("#issueSelect-textarea").attr("class", "select2-choices");
    
    selectedArray.forEach(function(element){
      jQuery("#issueSelect-textarea").append(element);
      jQuery("#issueSelect-textarea").append(" ");
    });
    ip.handleFreeInput();
    jQuery("#issueSelect-textarea").attr("tabindex", "1");
  }
  
  function popupCalendarsSetup() {
    var original = Calendar.prototype.show;
    Calendar.prototype.show = function() {
      original.call(this);
      setExcludeDaysToWeekend(jttp.options.excludeDays);
      setLoggedDaysDesign(jttp.options.isColoring, jttp.options.loggedDays);
    }
      var calPop = Calendar.setup({
        firstDay : jttp.options.firstDay,
        inputField : jQuery("#dateHidden"),
        button : jQuery("#jttp-headline-day-calendar"),
     //   date : new Date(millisTimeZoneCorrection(jttp.options.dateFormatted)).print(jttp.options.dateFormat),
        ifFormat: jttp.options.dateFormat,
        align : 'Br',
        electric : false,
        singleClick : true,
        showOthers : true,
        useISO8601WeekNumbers : jttp.options.useISO8601,
      });
  }
  
  jttp.toggleSummary = function() {
    var module = jQuery("#summaryModule");
    jQuery(".mod-content", module).toggle(0, function() {
        module.toggleClass("collapsed");
    });
  }
  
  function getWorklogValuesJson(){
    var issueKey = jQuery('#issueSelect').val() || [];
    var startTime = jQuery('#startTime').val() || "";
    var endOrDuration = jQuery('input[name="endOrDuration"]:checked').val();
    var endTime = jQuery('#endTime').val() || "";
    var durationTime = jQuery('#durationTime').val() || "";
    var comment = jQuery('#comments').val() ||"";
    var isDurationSelect = true;
    if(endOrDuration == "end"){
      isDurationSelect = false;
    }
    var remainingEstimateType = jQuery('#remainingEstimateType').val();
    var newEstimate = jQuery('#newEstimate').val();
    var adjustmentAmount = jQuery('#adjustmentAmount').val();
    var periodTmp =  jQuery('input[name="usePeriod"]:checked').val();
    var period = false
    if(periodTmp != null){
      period = true;
    }
    var endDatePicker = jQuery('#enddate').val();
    if(endDatePicker != ""){
      try{
        var endDate = Date.parseDate(endDatePicker, jttp.options.dateFormat);
        if(endDatePicker != endDate.print(jttp.options.dateFormat)){
          showErrorMessage("error_message_label_ed");
          return false;
        }
        var endDateMilis  = timeZoneCorrection(endDate);
      }catch(err){
        showErrorMessage("error_message_label_ed");
        return false;
      }
    }
    
    var worklogValues = {
      "startTime": startTime,
      "endTime": endTime,
      "durationTime": durationTime,
      "isDuration": isDurationSelect,
      "comment": comment,
      "issueKey": issueKey,
      "remainingEstimateType": remainingEstimateType,
      "newEstimate": newEstimate,
      "adjustmentAmount": adjustmentAmount,
      "endDate": endDateMilis,
      "period": period,
    }
    return worklogValues;
  }
  
  function showErrorMessage(message_key){
    jQuery('#jttp-panel-head-fields > div.aui-message.aui-message-error > label').hide();
    var errorMessageLabel = jQuery('#'+message_key);
    errorMessageLabel.show();
    var errorMessage = jQuery('#jttp-panel-head-fields > div.aui-message.aui-message-error');
    errorMessage.show();
  }
  
  function commentsCSSFormat() {
    var comment = jttp.options.comment;
    jQuery("#comments").append(comment);
    jQuery("#comments").attr("tabindex", "4");
    jQuery('#comments').attr("style","height: 85px;");
  }

  function calculateTimeForInputfileds(hour, min) {
    if (hour < 10) {
      hour = "0" + hour
    }
    if (min < 10) {
      min = "0" + min
    }
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A");
    var locale = AJS.Meta.get('user-locale');
    evmoment.locale(locale);
    var timeVal = evmoment('2000-10-10 ' + hour + ':' + min, 
        ['YYYY-MM-DD hh:mm'])
        .format(dateTime);
    return timeVal.toUpperCase();
  }

  function setLoggedDaysDesign(isColoring, loggedDays) {
    if (isColoring) {
      var dayNumber = loggedDays.length;
      for (var i = 0; i < dayNumber; i++) {
        var theDay = loggedDays[i];
        var calendarDays = jQuery('.day.day-' + theDay);
        for (var j = 0; j < calendarDays.length; j++) {
          if (!(jQuery(calendarDays[j]).hasClass('selected')
              || jQuery(calendarDays[j]).hasClass('othermonth') || jQuery(calendarDays[j])
              .hasClass('logged'))) {
            calendarDays[j].className += " logged";
          }
        }
      }
    }
  }

  function setExcludeDaysToWeekend(excludeDays) {
    var dayNumber = excludeDays.length;
    for (var i = 0; i < dayNumber; i++) {
      var date = new Date(excludeDays[i]);
      var theDay = date.getDate();
      var calendarDays = jQuery('.day.day-' + theDay);
      for (var j = 0; j < calendarDays.length; j++) {
        if (!(jQuery(calendarDays[j]).hasClass('selected')
            || jQuery(calendarDays[j]).hasClass('othermonth') || jQuery(calendarDays[j]).hasClass(
            'weekend'))) {
          calendarDays[j].className += " weekend";
        }
      }
    }
  }

  function setStartNow() {
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    jttp.startState = 1;
    var now = calculateTimeForInputfileds(hour, minute);
    jQuery("#startTime").val(now);
  }

  function setStartDecTemporary(startTimeChange) {
    setStartNow();
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A")
    var locale = AJS.Meta.get('user-locale');
    var statTimeDate = evmoment('2000-10-10 ' + jQuery("#startTime").val(), 'YYYY-MM-DD ' + dateTime, locale);

    var hour = parseInt(statTimeDate.hours());
    var minString = statTimeDate.minutes().toString();
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1, 2));
    if ((minSubInt != 0) && (minSubInt != 5)) {
      min = min - startTimeChange;
      if (minSubInt < 5) {
        min = min + (5 - minSubInt);
      } else if (minSubInt > 5) {
        min = min + (10 - minSubInt);
      }
      jttp.startState = 0;
    }
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#startTime").val(time);
  }

  function setStartInc(startTimeChange) {
    setStartNow();
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A")
    var locale = AJS.Meta.get('user-locale');
    var statTimeDate = evmoment('2000-10-10 ' + jQuery("#startTime").val(), 'YYYY-MM-DD ' + dateTime, locale);
    
    var hour = parseInt(statTimeDate.hours());
    var min = parseInt(statTimeDate.minutes());
    min = min + startTimeChange;
    if (min >= 60) {
      min = min - 60;
      hour = hour + 1;
      if (hour > 23) {
        hour = 0;
      }
    }
    var minString = min.toString();
    var minSubInt;
    if (minString.length > 1) {
      minSubInt = parseInt(minString.substring(1, 2));
    } else {
      minSubInt = min;
    }
    if (minSubInt >= 5) {
      min = min - (minSubInt - 5);
    } else {
      min = min - minSubInt;
    }
    jttp.startState = 2;
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#startTime").val(time);
  }

  function setEndNow() {
    var currentTime = new Date();
    var hour = currentTime.getHours();
    var minute = currentTime.getMinutes();
    jttp.endState = 1;
    var now = calculateTimeForInputfileds(hour, minute);
    jQuery("#endTime").val(now);
  }

  function setEndDecTemporary(endTimeChange) {
    setEndNow();
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A")
    var locale = AJS.Meta.get('user-locale');
    var endTimeDate = evmoment('2000-10-10 ' + jQuery("#endTime").val(), 'YYYY-MM-DD ' + dateTime, locale);
    
    var hour = parseInt(endTimeDate.hours());
    var minString = endTimeDate.minutes().toString();
    var min = parseInt(minString);
    var minSubInt = parseInt(minString.substring(1, 2));
    if ((minSubInt != 0) && (minSubInt != 5)) {
      min = min - endTimeChange;
      var checkHour = false;
      if (min < 0) {
        min = 60 + min;
        checkHour = true;
        minSubInt = parseInt(min.toString().substring(1, 2));
        ;
      }
      if (minSubInt < 5) {
        min = min + (5 - minSubInt);
      } else if (minSubInt > 5) {
        min = min + (10 - minSubInt);
      }
      if (checkHour) {
        if (min != 60) {
          hour = hour - 1;
          if (hour < 0) {
            hour = 23;
          }
        } else {
          min = 0;
        }
      }
      jttp.endState = 0;
    }
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#endTime").val(time);
  }

  function setEndInc(endTimeChange) {
    setEndNow();
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A")
    var locale = AJS.Meta.get('user-locale');
    var endTimeDate = evmoment('2000-10-10 ' + jQuery("#endTime").val(), 'YYYY-MM-DD ' + dateTime, locale);
    
    var hour = parseInt(endTimeDate.hours());
    var min = parseInt(endTimeDate.minutes());
    min = min + endTimeChange;
    if (min >= 60) {
      min = min - 60;
      hour = hour + 1;
      if (hour > 23) {
        hour = 0;
      }
    }
    var minString = min.toString();
    var minSubInt;
    if (minString.length > 1) {
      minSubInt = parseInt(minString.substring(1, 2));
    } else {
      minSubInt = min;
    }
    if (minSubInt >= 5) {
      min = min - (minSubInt - 5);
    } else {
      min = min - minSubInt;
    }
    jttp.endState = 2;
    var time = calculateTimeForInputfileds(hour, min);
    jQuery("#endTime").val(time);
  }
  
  jttp.selectCalculationType = function(){
    var $endInput = jQuery('#endTime');
    if($endInput.attr("disabled")){
      jttp.calculateEndTime();
    }else{
      jttp.calculateDuration();
    }
  }
  
  jttp.calculateDuration = function(){
    var $startInput = jQuery('#startTime');
    var $endInput = jQuery('#endTime');
    var $durationInput = jQuery('#durationTime');
    
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A")
    var locale = AJS.Meta.get('user-locale');
    var statTimeDate = evmoment('2000-10-10 ' + $startInput.val(), 'YYYY-MM-DD ' + dateTime, locale);
    var endTimeDate = evmoment('2000-10-10 ' + $endInput.val(), 'YYYY-MM-DD ' + dateTime, locale);
    
    var endTimeHour = parseInt(endTimeDate.hours());
    var endTimeMin = parseInt(endTimeDate.minutes());
    
    var startTimeHour = parseInt(statTimeDate.hours());
    var startTimeMin = parseInt(statTimeDate.minutes());
    
    var durationHour = endTimeHour - startTimeHour;
    var durationMin = endTimeMin - startTimeMin;
    
    if(durationMin < 0) {
      durationHour = durationHour - 1;
      durationMin = 60 + durationMin;
    }

    // startime is after endtime
    if(durationHour < 0) {
      $durationInput.val("");
      return false;
    }

    var durationHourString = String(durationHour);
    if(durationHour < 10) {
      durationHourString = "0" + String(durationHour);
    }
    
    var durationMinString = String(durationMin);
    if(durationMin < 10) {
      durationMinString = "0" + String(durationMin);
    }
    
    $durationInput.val(durationHourString + ":" + durationMinString);
  }

  jttp.calculateEndTime = function(){
    var $startInput = jQuery('#startTime');
    var $endInput = jQuery('#endTime');
    var $durationInput = jQuery('#durationTime');
    
    var dateTime = AJS.Meta.get('date-time');
    dateTime = dateTime.replace("aa","a");
    dateTime = dateTime.replace("AA","A")
    var locale = AJS.Meta.get('user-locale');
    var statTimeDate = evmoment('2000-10-10 ' + $startInput.val(), 'YYYY-MM-DD ' + dateTime, locale);

    var startTime = $startInput.val();
    var durationTime = $durationInput.val();
    
    if(durationTime.length != 5) {
      return false;
    }
    
    var durationTimeParts = durationTime.split(':');
    var durationTimeHour = parseInt(durationTimeParts[0]);
    var durationTimeMin = parseInt(durationTimeParts[1]);
    
    var startTimeHour = parseInt(statTimeDate.hours());
    var startTimeMin = parseInt(statTimeDate.minutes());
    
    var endHour = durationTimeHour + startTimeHour;
    var endMin = durationTimeMin + startTimeMin;
    
    if(endMin > 59) {
      endHour = endHour + 1;
      endMin = endMin - 60;
    }

    // startime is after endtime
    if(endHour > 23) {
      endHour = endHour - 24;
    }

    var endHourString = String(endHour);
    if(endHour < 10) {
      endHourString = "0" + String(endHour);
    }
    
    var endMinString = String(endMin);
    if(endMin < 10) {
      endMinString = "0" + String(endMin);
    }
    
    evmoment.locale(locale);
    var endTimeVal = evmoment('2000-10-10 ' + endHourString + ':' + endMinString, 
        ['YYYY-MM-DD hh:mm'])
        .format(dateTime);
    $endInput.val(endTimeVal.toUpperCase());
  }

  jttp.reamingEstimateChange = function(obj){
    var $obj = jQuery(obj);
    var type = $obj.attr('data-jttp-remaining-estimate-type');

    jQuery('#remainingEstimateType').val(type);

    jQuery('button[data-jttp-remaining-estimate-type]').children('span').hide();
    $obj.children('span').show();
    
    var $newEstimate = jQuery('#newEstimate');
    var $adjustmentAmount = jQuery('#adjustmentAmount');
    $newEstimate.attr('disabled', 'disabled');
    $adjustmentAmount.attr('disabled', 'disabled');
    if(type == 'NEW'){
      $newEstimate.removeAttr('disabled');
    }
    if(type == 'MANUAL'){
      $adjustmentAmount.removeAttr('disabled');
    }
  }

  jttp.showDeleteConfirmation = function(worklogId){
    jQuery('#actionWorklogIdForDelete').val(worklogId);
    AJS.dialog2('#delete_confirmation_dialog').show();
  }

  jttp.beforeSubmitDeleteAction = function() {
    var type = jQuery('input[name="deleteRemainingEstimateType"]:checked').val();
    if(typeof type == 'undefined' || type == '' || type == null){
      type = 'AUTO';
    }
    jQuery('#deleteRemainingEstimateType').val(type);

    var newEstimate = jQuery('input[name="delete_new_estimate"]').val();
    jQuery('#delete_new_estimate').val(newEstimate);

    var adjustmentAmount = jQuery('input[name="delete_adjustment_amount"]').val();
    jQuery('#delete_adjustment_amount').val(adjustmentAmount);

    var date = jQuery('#date');
    date.val(jttp.options.currentServerTime);
    jQuery("#actionFormForDelete").append(date);
    
    return true;
  }
  
})(everit.jttp.main, jQuery);
