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
everit.reporting = everit.reporting || {};
everit.reporting.main = everit.reporting.main || {};

(function(reporting, jQuery) {

  Date.prototype.format = function (formatString) {
    return fecha.format(this, formatString);
  };
  
  jQuery(document).ready(function() {
    fecha.i18n = {
        dayNamesShort: Calendar._SDN,
        dayNames: Calendar._DN,
        monthNamesShort: Calendar._SMN,
        monthNames: Calendar._MN,
        amPm: ['am', 'pm'],
        // D is the day of the month, function returns something like...  3rd or 11th
        DoFn: function (D) {
            return D + [ 'th', 'st', 'nd', 'rd' ][ D % 10 > 3 ? 0 : (D - D % 10 !== 10) * D % 10 ];
        }
    }
    
    var opt = reporting.values;
     
    Date.parseDate = function(str, fmt){
      return fecha.parse(str, AJS.Meta.get("date-dmy").toUpperCase());
      };
      
    jQuery("#dateFrom").val(fecha.format(opt.dateFromFormated, AJS.Meta.get("date-dmy").toUpperCase()));
    jQuery("#dateTo").val(fecha.format(opt.dateToFormated, AJS.Meta.get("date-dmy").toUpperCase()));
    
    var calFrom = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateFrom"),
      button : jQuery("#date_trigger_from"),
      date : opt.dateFromFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
      onSelect: reporting.onSelect,
    });

    var calTo = Calendar.setup({
      firstDay : opt.firstDay,
      inputField : jQuery("#dateTo"),
      button : jQuery("#date_trigger_to"),
      date : opt.dateToFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : opt.useISO8601,
      onSelect: reporting.onSelect,
    });

    jQuery('.aui-ss, .aui-ss-editing, .aui-ss-field').attr("style", "width: 300px;");
    
    initProjectSelect();
    initTypeSelect();
    initStatusSelect();
    initMoreSelect();
    
    initUserSelect();
    initGroupSelect();
    
    initWorklogDetailsColumns();
    
    //this three not use rest
    initCreatedDatePicker();
    initEpicNameSelect();
    initFilterSelect();
    
    tutorialDialogInit();
    
    if( reporting.values.notBrowsableProjectKeys.length ) {
      var keys = "";
      var length = reporting.values.notBrowsableProjectKeys.length;
      for( var i in reporting.values.notBrowsableProjectKeys ) {
        keys += reporting.values.notBrowsableProjectKeys[i];
        if( i < length - 1 ) {
          keys += ", ";
        }
      }
      AJS.messages.warning({
        title: "Some worklogs cannot be displayed",
        body: "You don't have the permission to browse the following projects: " + keys +"."
      });
    }

    addTooltips();
    
    reporting.changeFilterType(reporting.values.searcherValue);
  });
  
  
  var actualTutorialPage;

  function tutorialDialogInit() {
    if(reporting.values.isShowTutorial){
      AJS.dialog2("#reporting-tutorial-dialog").show();
      actualTutorialPage = 1
      changeNavigationButtonVisibility();
    }
  };
    

  reporting.tutorialDialogHide = function(){
    if(jQuery('#tutorial_dns:checked').length){
      return true;
    }else{
      AJS.dialog2("#reporting-tutorial-dialog").hide();
      return false;
    }
  };
  
  var morePickerShowFunctions = {
      "issuePicker-parent": function(){ 
            if(!jQuery('#issuePicker-multi-select').length){
             initIssueSelect();
            }
          },
      "priorityPickerButton": function(){
            if(!jQuery('#priorityPicker-suggestions').length){
              initPrioritySelect();
            }
          },
      "resolutionPickerButton": function(){
            if(!jQuery('#resolutionPicker-suggestions').length){
              initResolutionSelect();
            }
          },
      "assignePickerButton": function(){
            if(!jQuery('#assignePicker-suggestions').length){
              initAssigneSelect();
            }
          },
      "reporterPickerButton": function(){
            if(!jQuery('#reporterPicker-suggestions').length){
              initReporterSelect();
            }
          },
      "affectedVersionPickerButton": function(){
            if(! jQuery('#affectedVersionPicker-suggestions').length){
              initAffectedVersionSelect();
            }
          },
      "fixVersionPickerButton": function(){
            if(! jQuery('#fixVersionPicker-suggestions').length){
              initFixVersionSelect();
            }
          },
      "componentPickerButton": function(){
            if(! jQuery('#componentPicker-suggestions').length){
              initComponentSelect();
            }
          },
      "labelPickerButton": function(){
            if(! jQuery('#labelPicker-suggestions').length){
              initLabelSelect();
            }
          },
      "createdPickerButton": function(){
          },
      "epicNamePickerButton": function(){
          },
      "epicLinkPickerButton": function(){
            if(! jQuery('#epicLinkPicker-suggestions').length){
              initEpicLinkSelect();
            }
          },
    }
  
  var morePickerHideFunctions = {
      "issuePicker-parent": function(){ 
            jQuery("#issuePicker-multi-select .representation ul li em").click();
            jQuery("#issuePicker-textarea").css("padding-left", "0px");
          },
      "priorityPickerButton": function(){
            jQuery('#priorityPicker-suggestions input:checked').click();
            jQuery("#priorityPickerButton").text(AJS.I18n.getText("jtrp.picker.all.priority"));
          },
      "resolutionPickerButton": function(){
            jQuery('#resolutionPicker-suggestions input:checked').click();
            jQuery("#resolutionPickerButton").text(AJS.I18n.getText("jtrp.picker.all.resulution"));
          },
      "assignePickerButton": function(){
            jQuery('#assignePicker-suggestions input:checked').click();
            jQuery("#assignePickerButton").text(AJS.I18n.getText("jtrp.picker.all.assigne"));
          },
      "reporterPickerButton": function(){
            jQuery('#reporterPicker-suggestions input:checked').click();
            jQuery("#reporterPickerButton").text(AJS.I18n.getText("jtrp.picker.all.reporter"));
          },
      "affectedVersionPickerButton": function(){
            jQuery('#affectedVersionPicker-suggestions input:checked').click();
            jQuery("#affectedVersionPickerButton").text(AJS.I18n.getText("jtrp.picker.all.affects.version"));
          },
      "fixVersionPickerButton": function(){
            jQuery('#fixVersionPicker-suggestions input:checked').click();
            jQuery("#fixVersionPickerButton").text(AJS.I18n.getText("jtrp.picker.all.fix.version"));
          },
      "componentPickerButton": function(){
            jQuery('#componentPicker-suggestions input:checked').click();
            jQuery("#componentPickerButton").text(AJS.I18n.getText("jtrp.picker.all.component"));
          },
      "labelPickerButton": function(){
            jQuery('#labelPicker-suggestions input:checked').click();
            jQuery("#labelPickerButton").text(AJS.I18n.getText("jtrp.picker.all.label"));
          },
      "createdPickerButton": function(){
            jQuery("#createdPicker").val("");
            jQuery("#createdPickerButton").text(AJS.I18n.getText("jtrp.picker.all.create.date"));
          },
      "epicNamePickerButton": function(){
            jQuery("#epicNamePicker").val("");
            jQuery("#epicNamePickerButton").text(AJS.I18n.getText("jtrp.picker.all.epic.name"));
          },
      "epicLinkPickerButton": function(){
            jQuery('#epicLinkPicker-suggestions input:checked').click();
            jQuery("#epicNamePickerButton").text(AJS.I18n.getText("jtrp.picker.all.epic.link"));
          },
    }
    
  function initMoreSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedMore ); 
    var morePickerOptions = jQuery("#morePicker option");
    for (i = 0; i < morePickerOptions.length; i++){
      var optionValue = jQuery(morePickerOptions[i]).val();
      var selected = checkSelected(optionValue, selectedArray);
      if(selected == "selected"){
        jQuery(morePickerOptions[i]).attr("selected","selected");
        morePickerShowFunctions[optionValue]();
        jQuery("#" + optionValue).show();
      }
    }
    setMorePickerParentVisibility();
    var pp = new AJS.CheckboxMultiSelect({
      element:  jQuery("#morePicker"),
      submitInputVal: true,
    });
    pp._setDescriptorSelection = function(descriptor, $input) {
      var clickedOptionValue = descriptor.value();
      if (!descriptor.selected()) {
          this.selectItem(descriptor);
          $input.attr("checked", "checked");
          morePickerShowFunctions[clickedOptionValue]();
          jQuery("#" + clickedOptionValue).show();
          if(clickedOptionValue != "issuePicker-parent"){ 
            jQuery("#morePicker-parent").show();
          }
      } else {
          this.unselectItem(descriptor);
          $input.removeAttr("checked");
          jQuery("#" + clickedOptionValue).hide();
          morePickerHideFunctions[clickedOptionValue]();
          setMorePickerParentVisibility();
      }
    };
    pp.clear = function () {
      var instance = this;
      var selectedDescriptors = this.model.getDisplayableSelectedDescriptors();
      this.model.setAllUnSelected();
      if (this.$field.val().length === 0) {
          this.$field.val("");
          this.listController.$container.find(":checkbox").removeAttr("checked");
      } else {
          this.clearQueryField();
          this.listController.moveToFirst();
      }
      this._toggleClearButton();
      jQuery.each(selectedDescriptors, function() {
          instance.model.$element.trigger("unselect", [this, instance, true]);
          var clickedOptionValue = this.value();
          jQuery("#" + clickedOptionValue).hide();
          morePickerHideFunctions[clickedOptionValue]();
          setMorePickerParentVisibility();
      });
    };
    
  };

  function setMorePickerParentVisibility(){
     if(jQuery("#morePicker-parent .search-container.basic").find("a:visible").length > 0){
       jQuery("#morePicker-parent").show();
     }else{
       jQuery("#morePicker-parent").hide();
     }
  }
  
 function initCreatedDatePicker(){
    if(!isNaN(reporting.values.dateCreatedFormated)){
      jQuery("#createdPicker").val(fecha.format(reporting.values.dateCreatedFormated, AJS.Meta.get("date-dmy").toUpperCase()));
    }
    var createdDate = Calendar.setup({
      firstDay : reporting.values.firstDay,
      inputField : jQuery("#createdPicker"),
      button : jQuery("#createdPickerTrigger"),
      date : reporting.values.dateCreatedFormated,
      align : 'Br',
      electric : false,
      singleClick : true,
      showOthers : true,
      useISO8601WeekNumbers : reporting.values.useISO8601,
      onSelect: reporting.onSelect,
    });
    updateInputFieldPickButtonText("#createdPicker" , "#createdPickerButton",AJS.I18n.getText("jtrp.picker.all.create.date"));
    jQuery("#createdPicker").on("change onblur", function() {
      updateInputFieldPickButtonText("#createdPicker" , "#createdPickerButton",AJS.I18n.getText("jtrp.picker.all.create.date"));
    });
  }
  
  function initEpicNameSelect(){
    updateInputFieldPickButtonText("#epicNamePicker" , "#epicNamePickerButton", AJS.I18n.getText("jtrp.picker.all.epic.name"));
    jQuery("#epicNamePicker").on("change onblur", function() {
      updateInputFieldPickButtonText("#epicNamePicker" , "#epicNamePickerButton", AJS.I18n.getText("jtrp.picker.all.epic.name"));
    });
  };
 
  function initPrioritySelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedPriorities ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/priority",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          var avatarId =  obj.iconUrl;
          jQuery("#priorityPicker").append('<option data-icon="' + avatarId + '" value="'+obj.id + '" '+ selected + '>' +obj.name +'</option>');

        }
        var pp = new AJS.CheckboxMultiSelect({
            element:  jQuery("#priorityPicker"),
            submitInputVal: true,
        });
        updatePickerButtonText("#priorityPicker" , "#priorityPickerButton", AJS.I18n.getText("jtrp.picker.all.priority"));
        jQuery("#priorityPicker").on("change unselect", function() {
          updatePickerButtonText("#priorityPicker" , "#priorityPickerButton", AJS.I18n.getText("jtrp.picker.all.priority"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initProjectSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedProjects ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/project",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          var avatarId =  obj.avatarUrls["16x16"];
          jQuery("#projectPicker").append('<option data-icon="' + avatarId + '" value="' + obj.id + '" '+ selected +'>' +obj.name+ '(' + obj.key + ' )</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
            element:  jQuery("#projectPicker"),
            submitInputVal: true,
        });
        updatePickerButtonText("#projectPicker" , "#projectPickerButton", AJS.I18n.getText("jtrp.picker.all.project"));
        jQuery("#projectPicker").on("change unselect", function() {
          updatePickerButtonText("#projectPicker" , "#projectPickerButton", AJS.I18n.getText("jtrp.picker.all.project"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initAssigneSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedAssignes ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listUsers?pickerUserQueryType=ASSIGNEE",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var avatarId =  contextPath + "/secure/useravatar?size=xsmall&ownerId=" + obj.avatarOwner;
          var selected = checkSelected(obj.userName, selectedArray);
          jQuery("#assignePicker").append('<option data-icon="' + avatarId + '" value="'+obj.userName + '" '+ selected + '>' +obj.displayName +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
          element:  AJS.$("#assignePicker"),
          submitInputVal: true,
        });
        updatePickerButtonText("#assignePicker" , "#assignePickerButton", AJS.I18n.getText("jtrp.picker.all.assigne"));
        jQuery("#assignePicker").on("change unselect", function() {
          updatePickerButtonText("#assignePicker" , "#assignePickerButton", AJS.I18n.getText("jtrp.picker.all.assigne"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initReporterSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedReportes ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listUsers?pickerUserQueryType=REPORTER",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var avatarId =  contextPath + "/secure/useravatar?size=xsmall&ownerId=" + obj.avatarOwner;
          var selected = checkSelected(obj.userName, selectedArray);
          jQuery("#reporterPicker").append('<option data-icon="' + avatarId + '" value="'+obj.userName + '" '+ selected + '>' +obj.displayName +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
          element:  AJS.$("#reporterPicker"),
          submitInputVal: true,
        });
        updatePickerButtonText("#reporterPicker" , "#reporterPickerButton", AJS.I18n.getText("jtrp.picker.all.reporter"));
        jQuery("#reporterPicker").on("change unselect", function() {
          updatePickerButtonText("#reporterPicker" , "#reporterPickerButton", AJS.I18n.getText("jtrp.picker.all.reporter"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initUserSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedUsers ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listUsers",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var avatarId =  contextPath + "/secure/useravatar?size=xsmall&ownerId=" + obj.avatarOwner;
          var selected = checkSelected(obj.userName, selectedArray);
          jQuery("#userPicker").append('<option data-icon="' + avatarId + '" value="'+obj.userName + '" '+ selected + '>' +obj.displayName +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
          element:  AJS.$("#userPicker"),
          submitInputVal: true,
        });
        pp._setDescriptorSelection = function(descriptor, $input) {
          var descriptValue = descriptor.value();
          if (!descriptor.selected()) {
              this.selectItem(descriptor);
              $input.attr("checked", "checked");
              if(descriptValue == "none"){
                var triggerData = {type:"click",name:"UserSelectClick",fakeClick:true};
                jQuery('#userPicker-suggestions input[checked="checked"][value!="none"]').click();
                jQuery('#groupPicker-suggestions [value="-1"]').trigger(triggerData);
                jQuery("#groupPickerButton").attr("aria-disabled", false);
              }
          } else {
              this.unselectItem(descriptor);
              $input.removeAttr("checked");
          }
        };
        
        updatePickerButtonTextWithNone("#userPicker" , "#userPickerButton",  AJS.I18n.getText("jtrp.picker.all.user"),  AJS.I18n.getText("jtrp.picker.none.user"), "none");
        jQuery("#userPicker").on("change unselect", function() {
          updatePickerButtonTextWithNone("#userPicker" , "#userPickerButton", AJS.I18n.getText("jtrp.picker.all.user"),  AJS.I18n.getText("jtrp.picker.none.user"), "none");
        });
     
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
    
  function updatePickerButtonTextWithNone(picker, button, defaultText, noneText, noneValue){ //Example vallues: "#userPicker" , "#userPickerButton", "User: All", "User: None"
    //FIND and decide none checked
    var newButtonText = "";
   if(jQuery(picker+" [value="+ noneValue +"]").attr("selected") != "selected"){
     jQuery(picker).find("option:selected").each(function() {
       var optionText = AJS.$(this).text();
       if (newButtonText === '') {
         newButtonText = optionText;
       } else {
         newButtonText = newButtonText + "," + optionText;
       }
     });
     if (newButtonText === '') {
       newButtonText = defaultText;
     }else if(newButtonText.length > 16){
       newButtonText = newButtonText.substring(0, 12) + "...";
     }
   }else{
    
     newButtonText = noneText;
     jQuery(button).attr("aria-disabled", true);
   }
   jQuery(button).text(newButtonText);
  };
  
  function initGroupSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedGroups ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/groups/picker",
      data : [],
      success : function(result){
        //Add None before result parse
        var selected = checkSelected("-1", selectedArray);
        jQuery("#groupPicker").append('<option value="-1" '+ selected + '>' + AJS.I18n.getText('jtrp.picker.value.none') +'</option>');
        for( var i in result.groups) {
          var obj = result.groups[i];
          var selected = checkSelected(obj.name, selectedArray);
          jQuery("#groupPicker").append('<option value="'+obj.name + '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#groupPicker"),
              submitInputVal: true,
        });
        pp._setDescriptorSelection = function(descriptor, $input) {
          var descriptValue = descriptor.value();
          if (!descriptor.selected()) {
              this.selectItem(descriptor);
              $input.attr("checked", "checked");
              if(descriptValue == "-1"){
                var triggerData = {type:"click",name:"GroupSelectClick",fakeClick:true};
                jQuery('#groupPicker-suggestions input[checked="checked"][value!="-1"]').click();
                jQuery('#userPicker-suggestions [value="none"]').trigger(triggerData);
                jQuery("#userPickerButton").attr("aria-disabled", false);
              }
          } else {
              this.unselectItem(descriptor);
              $input.removeAttr("checked");
          }
        };
        
        updatePickerButtonTextWithNone("#groupPicker" , "#groupPickerButton",  AJS.I18n.getText("jtrp.picker.all.group"),  AJS.I18n.getText("jtrp.picker.none.group"), "-1");
        jQuery("#groupPicker").on("change unselect", function() {
          updatePickerButtonTextWithNone("#groupPicker" , "#groupPickerButton", AJS.I18n.getText("jtrp.picker.all.group"),  AJS.I18n.getText("jtrp.picker.none.group"), "-1");
        });
      
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initTypeSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedTypes ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/issuetype",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var avatarId =  obj.iconUrl;
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#typePicker").append('<option data-icon="' + avatarId + '" value="'+obj.id + '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#typePicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#typePicker" , "#typePickerButton", AJS.I18n.getText("jtrp.picker.all.type"));
        jQuery("#typePicker").on("change unselect", function() {
          updatePickerButtonText("#typePicker" , "#typePickerButton", AJS.I18n.getText("jtrp.picker.all.type"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initResolutionSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedResolutions ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/resolution",
      data : [],
      success : function(result){
        //Add NO RESULUTION before parse result
        var selected = checkSelected(-1, selectedArray);
        jQuery("#resolutionPicker").append('<option value="-1" '+ selected + '>' + AJS.I18n.getText('jtrp.picker.no.resulution.value') + '</option>');
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          jQuery("#resolutionPicker").append('<option value="'+obj.id+ '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#resolutionPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#resolutionPicker" , "#resolutionPickerButton", AJS.I18n.getText("jtrp.picker.all.resolution"));
        jQuery("#resolutionPicker").on("change unselect", function() {
          updatePickerButtonText("#resolutionPicker" , "#resolutionPickerButton", AJS.I18n.getText("jtrp.picker.all.resolution"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initStatusSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedStatus ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/api/2/status",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.id, selectedArray);
          var lozengeStatus = JSON.stringify(obj).replace(/"/g, "&quot;");
          jQuery("#statusPicker").append('<option value="'+obj.id+ '" data-simple-status="' + lozengeStatus +'"  ' + selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelectStatusLozenge({
              element:  AJS.$("#statusPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#statusPicker" , "#statusPickerButton", AJS.I18n.getText("jtrp.picker.all.status"));
        jQuery("#statusPicker").on("change unselect", function() {
          updatePickerButtonText("#statusPicker" , "#statusPickerButton", AJS.I18n.getText("jtrp.picker.all.status"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initAffectedVersionSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedAffectedVersions ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listVersions?pickerVersionQueryType=AFFECTED_VERSION",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.name, selectedArray);
          jQuery("#affectedVersionPicker").append('<option value="'+obj.name+ '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#affectedVersionPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#affectedVersionPicker" , "#affectedVersionPickerButton", AJS.I18n.getText("jtrp.picker.all.affects.version"));
        jQuery("#affectedVersionPicker").on("change unselect", function() {
          updatePickerButtonText("#affectedVersionPicker" , "#affectedVersionPickerButton", AJS.I18n.getText("jtrp.picker.all.affects.version"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initFixVersionSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedFixVersions ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listVersions?pickerVersionQueryType=FIX_VERSION", 
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.name, selectedArray);
          jQuery("#fixVersionPicker").append('<option value="'+obj.name+ '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#fixVersionPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#fixVersionPicker" , "#fixVersionPickerButton", AJS.I18n.getText("jtrp.picker.all.fix.version"));
        jQuery("#fixVersionPicker").on("change unselect", function() {
          updatePickerButtonText("#fixVersionPicker" , "#fixVersionPickerButton", AJS.I18n.getText("jtrp.picker.all.fix.version"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
   reporting.prevTutorialPage = function(){
    if(actualTutorialPage > 1){
      actualTutorialPage--;
    }else{
      actualTutorialPage = 1;
    }
    changeNavigationButtonVisibility();
    showActiveTutotialPage();
   };

   reporting.nextTutorialPage = function(){
     if(actualTutorialPage < 5){
       actualTutorialPage++;
     }else{
       actualTutorialPage = 5;
      }
      changeNavigationButtonVisibility();
      showActiveTutotialPage();
   };
   
   function changeNavigationButtonVisibility(){
     if(actualTutorialPage == 1){
       jQuery("#reporting-tutorial-prev").hide();
     }else{
       jQuery("#reporting-tutorial-prev").show();
     }
     if(actualTutorialPage == 5){
       jQuery("#reporting-tutorial-next").hide();
     }else{
       jQuery("#reporting-tutorial-next").show();
     }
   }
   
   function showActiveTutotialPage(){
     jQuery("#reporting-tutorial-dialog .tabs-pane.active-pane").removeClass("active-pane");
     jQuery("#tutorialPage-"+ actualTutorialPage).addClass("active-pane");
   };
  
  function initLabelSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedLabels ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listLabels",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.name, selectedArray);
          jQuery("#labelPicker").append('<option value="'+obj.name+ '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#labelPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#labelPicker" , "#labelPickerButton",AJS.I18n.getText("jtrp.picker.all.label"));
        jQuery("#labelPicker").on("change unselect", function() {
          updatePickerButtonText("#labelPicker" , "#labelPickerButton", AJS.I18n.getText("jtrp.picker.all.label"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
    
  function initIssueSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedIssues ); 
    var ip = new AJS.IssuePicker({
      element : jQuery("#issuePicker"),
      userEnteredOptionsMsg : AJS.params.enterIssueKey,
      uppercaseUserEnteredOnSelect : true,
      singleSelectOnly : false,
      removeOnUnSelect: true
    });

    jQuery("#issuePicker-multi-select").attr("style", "width: 350px;");
    jQuery("#issuePicker-textarea").attr("style", "width: 350px;");
    jQuery("#issuePicker-textarea").attr("class", "select2-choices medium-field criteria-dropdown-textarea");
    jQuery(".issue-picker-popup").remove();
    
    selectedArray.forEach(function(element){
      jQuery("#issuePicker-textarea").append(element);
      jQuery("#issuePicker-textarea").append(" ");
    });
    ip.handleFreeInput();
  };
  function initFilterSelect(){
    var selectedFilterOption = jQuery('#filterPicker [value="'+ reporting.values.selectedFilter +'"]');
    selectedFilterOption.attr("selected","selected");
    var pp = new AJS.SingleSelect({
      element:  AJS.$("#filterPicker"),
      submitInputVal: false,
    });
    jQuery("#filterPicker-field").attr("class", "text medium-field criteria-dropdown-text");
    updatePickerButtonText("#filterPicker" , "#filterPickerButton", AJS.I18n.getText("jtrp.picker.none.filter"));
    jQuery("#filterPicker").on("change unselect", function() {
      updatePickerButtonText("#filterPicker" , "#filterPickerButton", AJS.I18n.getText("jtrp.picker.none.filter"));
    });
  };
  
  function initEpicLinkSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedEpicLinks ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listEpicLinks", 
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.epicLinkId, selectedArray);
          jQuery("#epicLinkPicker").append('<option value="'+obj.epicLinkId+ '" '+ selected + '>' +obj.epicName + ' - ('+ obj.issueKey +') </option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#epicLinkPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#epicLinkPicker" , "#epicLinkPickerButton", AJS.I18n.getText("jtrp.picker.all.epic.link"));
        jQuery("#epicLinkPicker").on("change unselect", function() {
          updatePickerButtonText("#epicLinkPicker" , "#epicLinkPickerButton", AJS.I18n.getText("jtrp.picker.all.epic.link"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function initComponentSelect(){
    var selectedArray =  jQuery.makeArray( reporting.values.selectedComponents ); 
    jQuery.ajax({
      async: true,
      type: 'GET',
      url : contextPath + "/rest/jttp-rest/1/picker/listComponents",
      data : [],
      success : function(result){
        for( var i in result) {
          var obj = result[i];
          var selected = checkSelected(obj.name, selectedArray);
          jQuery("#componentPicker").append('<option value="'+obj.name+ '" '+ selected + '>' +obj.name +'</option>');
        }
        var pp = new AJS.CheckboxMultiSelect({
              element:  AJS.$("#componentPicker"),
              submitInputVal: true,
        });
        updatePickerButtonText("#componentPicker" , "#componentPickerButton", AJS.I18n.getText("jtrp.picker.all.component"));
        jQuery("#componentPicker").on("change unselect", function() {
          updatePickerButtonText("#componentPicker" , "#componentPickerButton", AJS.I18n.getText("jtrp.picker.all.component"));
        });
      },
      error : function(XMLHttpRequest, status, error){
      }
    });
  };
  
  function checkSelected(id , selectedArray){
    var selected = "";
    for(var i in selectedArray){
      if(selectedArray[i] == id) selected = "selected";
    }
    return selected;
  };
  
  function updateInputFieldPickButtonText(picker, button, defaultText){ //Example vallues: "#epicNamePicker" , "#epicNameButton", "Epic Name: All"
    var newButtonText =  jQuery(picker).val();
    if (newButtonText === '') {
      newButtonText = defaultText;
    }else if(newButtonText.length > 16){
      newButtonText = newButtonText.substring(0, 12) + "...";
    }
    jQuery(button).text(newButtonText);
  };
  
  function updatePickerButtonText(picker, button, defaultText){ //Example vallues: "#projectPicker" , "#projectPickerButton", "Project: All"
    var newButtonText = "";
    jQuery(picker).find("option:selected").each(function() {
      var optionText = AJS.$(this).text();
      if (newButtonText === '') {
        newButtonText = optionText;
      } else {
        newButtonText = newButtonText + "," + optionText;
      }
    });
    if (newButtonText === '') {
      newButtonText = defaultText;
    }else if(newButtonText.length > 16){
      newButtonText = newButtonText.substring(0, 12) + "...";
    }
    jQuery(button).text(newButtonText);
  };
  
  reporting.onSelect = function(cal) {
    //Copy of the original onSelect. Only chacnge not use te p.ifFormat
    var p = cal.params;
    var update = (cal.dateClicked || p.electric);
    if (update && p.inputField) {
      var dmy = AJS.Meta.get("date-dmy").toUpperCase();
      p.inputField.value = cal.date.format(dmy);
      jQuery(p.inputField).change();            
    }
    if (update && p.displayArea)
      p.displayArea.innerHTML = cal.date.print(p.daFormat);
    if (update && typeof p.onUpdate == "function")
      p.onUpdate(cal);
    if (update && p.flat) {
      if (typeof p.flatCallback == "function")
        p.flatCallback(cal);
    }
        if (p.singleClick === "true") {
            p.singleClick = true;
        } else if (p.singleClick === "false") {
            p.singleClick = false;
        }
    if (update && p.singleClick && cal.dateClicked)
      cal.callCloseHandler();
  }
  
  reporting.toggleModContent = function(type) {
    var module = jQuery("#" + type + "Module");
    jQuery(".mod-content", module).toggle(0, function() {
        module.toggleClass("collapsed");
    });
  }

  reporting.changeFilterType = function(type) {
    var searchWrap = jQuery(".search-wrap");
    var formType = jQuery("input[name=formType]");
    
    formType.attr("value", type);
    
    if(type === "basic") {
        searchWrap.removeClass("filter").addClass("basic");
        setMorePickerParentVisibility();
        jQuery("#morePicker-parent").css("margin-top","10px");
        jQuery("#issuePicker-parent").css("margin-top","10px");
    } else {
        searchWrap.removeClass("basic").addClass("filter");
        jQuery("#morePicker-parent").show();
        jQuery("#morePicker-parent").css("margin-top","0px");
        jQuery("#issuePicker-parent").css("margin-top","0px");
    }
  }
  
  function getFilterConditionJson(){
    var issueAssignees = jQuery('#assignePicker').val() || [];
    
    var projectIds = jQuery('#projectPicker').val() || [];
    if(projectIds){
      projectIds = projectIds.map(function(item) {
        return parseInt(item, 10);
      });
    }
    var issueTypeIds = jQuery('#typePicker').val() || [];
    
    var issueStatusIds = jQuery('#statusPicker').val() || [];
    
    var issuePriorityIds = jQuery('#priorityPicker').val() || [];
    
    var issueResolutionIds = jQuery('#resolutionPicker').val() || [];
    
    var issueReporters = jQuery('#reporterPicker').val() || [];
    
    var issueAffectedVersions = jQuery('#affectedVersionPicker').val() || [];
    
    var issueFixedVersions = jQuery('#fixVersionPicker').val() || [];
    
    var issueComponents = jQuery('#componentPicker').val() || [];
    var nocomponentIndex = jQuery.inArray("No component", issueFixedVersions);
    var selectNoComponentIssue = false;
    if(nocomponentIndex > -1) {
      issueFixedVersions.splice(nocomponentIndex, 1);
      selectNoComponentIssue = true;
    }

    var labels = jQuery('#labelPicker').val() || [];
    
    var issueEpicName = jQuery('#epicNamePicker').val();
    
    var issueEpicLinkIssueIds = jQuery('#epicLinkPicker').val() || [];
    if(issueEpicLinkIssueIds) {
      issueEpicLinkIssueIds = issueEpicLinkIssueIds.map(function(item) {
        return parseInt(item, 10);
      });
    }
    
    var groups = jQuery('#groupPicker').val() || [];
    
    var users =jQuery('#userPicker').val() || [];
    
    var issueKeys = jQuery('#issuePicker').val() || [];
    
    var filter = jQuery('#filterPicker').val() || [];
    
    var searcherValue = jQuery('#formType').val();
    
    var filterCondition = {
      "groups": groups,
      "issueAffectedVersions": issueAffectedVersions,
      "issueAssignees": issueAssignees,
      "issueComponents": issueComponents,
      "issueEpicLinkIssueIds": issueEpicLinkIssueIds,
      "issueEpicName": issueEpicName,
      "issueFixedVersions": issueFixedVersions,
      "issueKeys": issueKeys,
      "issuePriorityIds": issuePriorityIds,
      "issueReporters": issueReporters,
      "issueResolutionIds": issueResolutionIds,
      "issueStatusIds": issueStatusIds,
      "issueTypeIds": issueTypeIds,
      "labels": labels,
      "projectIds": projectIds,
      "users": users,
      "filter": filter,
      "searcherValue": searcherValue,
    }
    return filterCondition;
  }
  
  reporting.updateDetailsAllExportHref = function() {
    var filterCondition = getFilterConditionJson();
    var createdPicker = jQuery('#createdPicker').val();
    if(createdPicker != ""){
      try{
        var issueCreateDate = fecha.parse(createdPicker,  AJS.Meta.get("date-dmy").toUpperCase());
        var issueCreateDateMilis = issueCreateDate.getTime();
      }catch(err){
        showErrorMessage("error_message_label_cd");
        return false;
      }
    }
    filterCondition["issueCreateDate"] = issueCreateDateMilis;
    try{
      var dateFrom = jQuery('#dateFrom').val();
      var worklogStartDate = fecha.parse(dateFrom,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogStartDate"] = worklogStartDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_df");
      return false;
    }
    try{
      var dateTo = jQuery('#dateTo').val();
      var worklogEndDate = fecha.parse(dateTo,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogEndDate"] = worklogEndDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_dt");
      return false;
    }
    var downloadWorklogDetailsParam = {
        "filterCondition": filterCondition,
        "selectedWorklogDetailsColumns": reporting.values.worklogDetailsAllColumns
    }
    var json = JSON.stringify(downloadWorklogDetailsParam);
    var $detailsAllExport = jQuery('#detials-all-export')
    var href = $detailsAllExport.attr('data-jttp-href');
    $detailsAllExport.attr('href', href + '?json=' + json);
    return true;
  }
  
  reporting.updateDetailsCustomExportHref = function() {
    var filterCondition = getFilterConditionJson();
    var createdPicker = jQuery('#createdPicker').val();
    if(createdPicker != ""){
      try{
        var issueCreateDate = fecha.parse(createdPicker,  AJS.Meta.get("date-dmy").toUpperCase());
        var issueCreateDateMilis = issueCreateDate.getTime();
      }catch(err){
        showErrorMessage("error_message_label_cd");
        return false;
      }
    }
    filterCondition["issueCreateDate"] = issueCreateDateMilis;
    try{
      var dateFrom = jQuery('#dateFrom').val();
      var worklogStartDate = fecha.parse(dateFrom,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogStartDate"] = worklogStartDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_df");
      return false;
    }
    try{
      var dateTo = jQuery('#dateTo').val();
      var worklogEndDate = fecha.parse(dateTo,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogEndDate"] = worklogEndDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_dt");
      return false;
    }
    var selectedWorklogDetailsColumns = collectSelectedWorklogDetailsColumns();
    var downloadWorklogDetailsParam = {
        "filterCondition": filterCondition,
        "selectedWorklogDetailsColumns": selectedWorklogDetailsColumns
    }
    var json = JSON.stringify(downloadWorklogDetailsParam);
    var $detailsCustomExport = jQuery('#detials-custom-export')
    var href = $detailsCustomExport.attr('data-jttp-href');
    $detailsCustomExport.attr('href', href + '?json=' + json);
    return true;
  }
  
  reporting.updateSummariesExportHref = function() {
    var filterCondition = getFilterConditionJson();
    var createdPicker = jQuery('#createdPicker').val();
    if(createdPicker != ""){
      try{
        var issueCreateDate = fecha.parse(createdPicker,  AJS.Meta.get("date-dmy").toUpperCase());
        var issueCreateDateMilis = issueCreateDate.getTime();
      }catch(err){
        showErrorMessage("error_message_label_cd");
        return false;
      }
    }
    filterCondition["issueCreateDate"] = issueCreateDateMilis;
    try{
      var dateFrom = jQuery('#dateFrom').val();
      var worklogStartDate = fecha.parse(dateFrom,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogStartDate"] = worklogStartDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_df");
      return false;
    }
    try{
      var dateTo = jQuery('#dateTo').val();
      var worklogEndDate = fecha.parse(dateTo,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogEndDate"] = worklogEndDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_dt");
      return false;
    }
    var json = JSON.stringify(filterCondition);
    var $detailsCustomExport = jQuery('#summaries-export')
    var href = $detailsCustomExport.attr('data-jttp-href');
    $detailsCustomExport.attr('href', href + '?json=' + json);
    return true;
  }
  
  function showErrorMessage(message_key){
    AJS.$('#error_message label').hide();
    var errorMessageLabel = AJS.$('#'+message_key);
    errorMessageLabel.show();
    var errorMessage = AJS.$('#error_message');
    errorMessage.show();
  }
  
  reporting.beforeSubmitCreateReport = function() {
    var searcherValue = jQuery('#formType').val();
    if(searcherValue == "filter"){
      if(jQuery('#morePicker-parent div.error').length){
        return false;
      }
    }
    var filterCondition = getFilterConditionJson();
    var createdPicker = jQuery('#createdPicker').val();
    if(createdPicker != ""){
      try{
        var issueCreateDate = fecha.parse(createdPicker,  AJS.Meta.get("date-dmy").toUpperCase());
        var issueCreateDateMilis = issueCreateDate.getTime();
      }catch(err){
        showErrorMessage("error_message_label_cd");
        return false;
      }
    }
    filterCondition["issueCreateDate"] = issueCreateDateMilis;
    try{
      var dateFrom = jQuery('#dateFrom').val();
      var worklogStartDate = fecha.parse(dateFrom,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogStartDate"] = worklogStartDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_df");
      return false;
    }
    try{
      var dateTo = jQuery('#dateTo').val();
      var worklogEndDate = fecha.parse(dateTo,  AJS.Meta.get("date-dmy").toUpperCase());
      filterCondition["worklogEndDate"] = worklogEndDate.getTime();
    }catch(err){
      showErrorMessage("error_message_label_dt");
      return false;
    }
    filterCondition["limit"] = reporting.values.pageSizeLimit;
    filterCondition["offset"] = 0;
    var json = JSON.stringify(filterCondition);
    var $filterConditionJson = jQuery('#filterConditionJson')
    $filterConditionJson.val(json);
    
    var selectedWorklogDetailsColumns = collectSelectedWorklogDetailsColumns();
    var columnsJson = JSON.stringify(selectedWorklogDetailsColumns);
    jQuery('#selectedWorklogDetailsColumns').val(columnsJson);
    
    var selectedMore = jQuery('#morePicker').val();
    jQuery('#selectedMoreJson').val('[' + selectedMore + ']');
    
    var activeTabId = jQuery('.active-pane').attr('id');
    jQuery('#selectedActiveTab').val(activeTabId);
    
    var collapsedDetailsModuleVal = jQuery('#detailsModule').hasClass('collapsed');
    jQuery('#collapsedDetailsModule').val(collapsedDetailsModuleVal);
    
    var collapsedSummaryModuleVal = jQuery('#summaryModule').hasClass('collapsed');
    jQuery('#collapsedSummaryModule').val(collapsedSummaryModuleVal);
    
    jQuery('#reporting-result').addClass('pending');
    var $createReportButton = jQuery('#create-report-button');
    $createReportButton.attr('disabled', 'disabled');
    $createReportButton.attr('aria-disabled', 'true');
    jQuery('.create-report-button-spinner').spin('small');
    return true;
  }
  
  reporting.getWorklogDetailsPage = function(offset) {
    var url = contextPath + "/rest/jttp-rest/1/paging-report/pageWorklogDetails?filterConditionJson=";
    var filterConditionJson = jQuery('#filterConditionJson').val();
    var filterCondition = JSON.parse(filterConditionJson);
    filterCondition["offset"] = offset;
    var filterConditionJson = JSON.stringify(filterCondition);
    var selectedWorklogDetailsColumns = collectSelectedWorklogDetailsColumns();
    var selectedColumnsJson = JSON.stringify(selectedWorklogDetailsColumns);
    var $detailsModule = jQuery('#detailsModule');
    $detailsModule.addClass("pending");
    jQuery.get(url + filterConditionJson + "&selectedColumnsJson=" + selectedColumnsJson, function(data) {
      $detailsModule.replaceWith(data);
    }).done(function() {
      initWorklogDetailsColumns();
      $detailsModule.removeClass("pending");
      addTooltips();
    });
  }
  
  reporting.getProjectSummaryPage = function(offset) {
    var url = contextPath + "/rest/jttp-rest/1/paging-report/pageProjectSummary?filterConditionJson=";
    var filterConditionJson = jQuery('#filterConditionJson').val();
    var filterCondition = JSON.parse(filterConditionJson);
    filterCondition["offset"] = offset;
    var filterConditionJson = JSON.stringify(filterCondition);
    var $summaryModule = jQuery("#summaryModule");
    $summaryModule.addClass("pending");
    jQuery.get(url + filterConditionJson, function(data) {
      jQuery('#tabs-project-content').replaceWith(data);
    }).done(function(){
      $summaryModule.removeClass("pending");
      addTooltips();
    });
  }
  
  reporting.getIssueSummaryPage = function(offset) {
    var url = contextPath + "/rest/jttp-rest/1/paging-report/pageIssueSummary?filterConditionJson=";
    var filterConditionJson = jQuery('#filterConditionJson').val();
    var filterCondition = JSON.parse(filterConditionJson);
    filterCondition["offset"] = offset;
    var filterConditionJson = JSON.stringify(filterCondition);
    var $summaryModule = jQuery("summaryModule");
    $summaryModule.addClass("pending");
    jQuery.get(url + filterConditionJson, function(data) {
      jQuery('#tabs-issue-content').replaceWith(data);
    }).done(function(){
      $summaryModule.removeClass("pending");
      addTooltips();
    });
  }
  
  reporting.getUserSummaryPage = function(offset) {
    var url = contextPath + "/rest/jttp-rest/1/paging-report/pageUserSummary?filterConditionJson=";
    var filterConditionJson = jQuery('#filterConditionJson').val();
    var filterCondition = JSON.parse(filterConditionJson);
    filterCondition["offset"] = offset;
    var filterConditionJson = JSON.stringify(filterCondition);
    var $summaryModule = jQuery("summaryModule");
    $summaryModule.addClass("pending");
    jQuery.get(url + filterConditionJson, function(data) {
      jQuery('#tabs-user-content').replaceWith(data);
    }).done(function(){
      $summaryModule.removeClass("pending");
      addTooltips();
    });
  }
  
  function initWorklogDetailsColumns(){
    var selectedArray =  reporting.values.worklogDetailsColumns; 
    var $options = jQuery("#detailsColumns option");
    for (i = 0; i < $options.length; i++){
      var $option = jQuery($options[i]);
      var optionValue = $option.val();
      var selected = checkSelected(optionValue, selectedArray);
      if(selected == "selected"){
        $option.attr("selected","selected");
        jQuery("." + optionValue).show();
      }
    }
    var pp = new AJS.CheckboxMultiSelect({
      element:  jQuery("#detailsColumns"),
      submitInputVal: true,
    });
    
    pp._setDescriptorSelection = function(descriptor, $input) {
      var clickedOptionValue = descriptor.value();
      if (!descriptor.selected()) {
          this.selectItem(descriptor);
          $input.attr("checked", "checked");
          jQuery("." + clickedOptionValue).show();
          reporting.values.worklogDetailsColumns.push(clickedOptionValue);
      } else {
          this.unselectItem(descriptor);
          $input.removeAttr("checked");
          var index = reporting.values.worklogDetailsColumns.indexOf(clickedOptionValue);
          if(index > 0) {
            reporting.values.worklogDetailsColumns.splice(index, 1);
          }
          jQuery("." + clickedOptionValue).hide();
      }
    };
    pp.clear = function () {
      var instance = this;
      var selectedDescriptors = this.model.getDisplayableSelectedDescriptors();
      this.model.setAllUnSelected();
      if (this.$field.val().length === 0) {
          this.$field.val("");
          this.listController.$container.find(":checkbox").removeAttr("checked");
      } else {
          this.clearQueryField();
          this.listController.moveToFirst();
      }
      this._toggleClearButton();
      jQuery.each(selectedDescriptors, function() {
          instance.model.$element.trigger("unselect", [this, instance, true]);
          var clickedOptionValue = this.value();
          var index = reporting.values.worklogDetailsColumns.indexOf(clickedOptionValue);
          if(index > 0) {
            reporting.values.worklogDetailsColumns.splice(index, 1);
          }
          jQuery("." + clickedOptionValue).hide();
      });
    };
  };
  
  function collectSelectedWorklogDetailsColumns() {
    return jQuery('#detailsColumns').val() || [];
  }
  
  function addTooltips(){
    var $projectExpectedTooltip = AJS.$('#userPickerButton');
    if(!$projectExpectedTooltip.hasClass('jtrp-tooltipped')) {
      $projectExpectedTooltip.tooltip();
      $projectExpectedTooltip.addClass('jtrp-tooltipped');
    }
    
    var $projectExpectedTooltip = AJS.$('#groupPickerButton');
    if(!$projectExpectedTooltip.hasClass('jtrp-tooltipped')) {
      $projectExpectedTooltip.tooltip();
      $projectExpectedTooltip.addClass('jtrp-tooltipped');
    }
    
    var $projectExpectedTooltip = AJS.$('#project-expected-tooltip');
    if(!$projectExpectedTooltip.hasClass('jtrp-tooltipped')) {
      $projectExpectedTooltip.tooltip();
      $projectExpectedTooltip.addClass('jtrp-tooltipped');
    }
    
    var $issueExpectedTooltip = AJS.$('#issue-expected-tooltip');
    if(!$issueExpectedTooltip.hasClass('jtrp-tooltipped')) {
      $issueExpectedTooltip.tooltip();
      $issueExpectedTooltip.addClass('jtrp-tooltipped');
    }
    
    var $jtrp_col_priorityTooltip = AJS.$('#jtrp_col_priority');
    if(!$jtrp_col_priorityTooltip.hasClass('jtrp-tooltipped')) {
      $jtrp_col_priorityTooltip.tooltip();
      $jtrp_col_priorityTooltip.addClass('jtrp-tooltipped');
    }
    
    var $jtrp_col_typeTooltip = AJS.$('#jtrp_col_type');
    if(!$jtrp_col_typeTooltip.hasClass('jtrp-tooltipped')) {
      $jtrp_col_typeTooltip.tooltip();
      $jtrp_col_typeTooltip.addClass('jtrp-tooltipped');
    }
    
    var $jtrp_col_priorityTooltip = AJS.$('#is_jtrp_col_priority');
    if(!$jtrp_col_priorityTooltip.hasClass('jtrp-tooltipped')) {
      $jtrp_col_priorityTooltip.tooltip();
      $jtrp_col_priorityTooltip.addClass('jtrp-tooltipped');
    }
    
    var $jtrp_col_typeTooltip = AJS.$('#is_jtrp_col_type');
    if(!$jtrp_col_typeTooltip.hasClass('jtrp-tooltipped')) {
      $jtrp_col_typeTooltip.tooltip();
      $jtrp_col_typeTooltip.addClass('jtrp-tooltipped');
    }
    
    AJS.$('.img-tooltip').each(function() {
      var $element = AJS.$(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip();
        $element.addClass('jtrp-tooltipped');
      }
    });
    
    AJS.$('.user-tooltip').each(function() {
      var $element = AJS.$(this);
      if(!$element.hasClass('jtrp-tooltipped')) {
        $element.tooltip();
        $element.addClass('jtrp-tooltipped');
      }
    });
  }
  
})(everit.reporting.main, jQuery);