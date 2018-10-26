/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package forms.chatbot.web.portlet.actions;

import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormInstance;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecordVersion;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordLocalService;
import com.liferay.dynamic.data.mapping.service.DDMFormInstanceRecordVersionLocalService;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.util.DDMFormValuesMerger;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import forms.chatbot.web.constants.FormsChatbotWebPortletKeys;
import forms.chatbot.web.constants.FormsChatbotWebWebKeys;
import forms.chatbot.web.util.DefaultDDMFormValuesFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Rafael Praxedes
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + FormsChatbotWebPortletKeys.FORMS_CHATBOT_WEB,
		"mvc.command.name=saveFormEntry"
	},
	service = MVCResourceCommand.class
)
public class SaveFormEntryMVCResourceCommand extends BaseMVCResourceCommand {

	protected void addDDMFormFieldValue(
		DDMForm ddmForm, DDMFormValues ddmFormValues, String fieldName,
		String fieldValue) {

		Map<String, DDMFormField> ddmFormFieldsMap =
			ddmForm.getDDMFormFieldsMap(false);

		DDMFormField ddmFormField = ddmFormFieldsMap.get(fieldName);

		if (ddmFormField == null || ddmFormField.isTransient()) {
			return;
		}

		DDMFormFieldValue ddmFormFieldValue = new DDMFormFieldValue();

		ddmFormFieldValue.setName(ddmFormField.getName());

		LocalizedValue value = new LocalizedValue(ddmForm.getDefaultLocale());

		value.addString(ddmForm.getDefaultLocale(), fieldValue);

		ddmFormFieldValue.setValue(value);

		ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);
	}

	protected DDMFormValues buildDDMFormValues(
		ResourceRequest resourceRequest, DDMForm ddmForm) throws Exception {

		String fieldName = ParamUtil.getString(resourceRequest, "fieldName");

		DDMFormValues ddmFormValues = new DDMFormValues(ddmForm);

		if (Validator.isNotNull(fieldName)) {
			String fieldValue = ParamUtil.getString(
				resourceRequest, "fieldValue");

			addDDMFormFieldValue(ddmForm, ddmFormValues, fieldName, fieldValue);
		}
		else {
			InputStream portletInputStream = resourceRequest.getPortletInputStream();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			StreamUtil.transfer(portletInputStream, outputStream);

			String answerString = new String(outputStream.toByteArray());

			JSONObject bodyJSONObject =
					jsonFactory.createJSONObject(answerString);

			JSONArray answersJSONArray = bodyJSONObject.getJSONArray("answers");

			for (int i = 0; i < answersJSONArray.length(); i++) {
				JSONObject answerJSONObject = answersJSONArray.getJSONObject(i);

				addDDMFormFieldValue(
						ddmForm, ddmFormValues, answerJSONObject.getString("id"),
						answerJSONObject.getString("value"));
			}
		}

		return ddmFormValues;
	}

	protected DDMFormValues createDDMFormValues(
			ResourceRequest resourceRequest, DDMForm ddmForm,
			DDMFormInstanceRecordVersion ddmFormInstanceRecordVersion)
		throws Exception {

		DDMFormValues ddmFormValues = null;

		DDMFormValues newDDMFormValues = buildDDMFormValues(
			resourceRequest, ddmForm);

		if (ddmFormInstanceRecordVersion != null) {
			ddmFormValues = ddmFormValuesMerger.merge(
				newDDMFormValues,
				ddmFormInstanceRecordVersion.getDDMFormValues());
		}
		else {
			DefaultDDMFormValuesFactory ddmFormValuesFactory =
				new DefaultDDMFormValuesFactory(
					ddmForm, ddmForm.getDefaultLocale());

			ddmFormValues = ddmFormValuesMerger.merge(
				newDDMFormValues, ddmFormValuesFactory.create());
		}

		return ddmFormValues;
	}

	protected ServiceContext createServiceContext(
			ResourceRequest resourceRequest)
		throws PortalException {

		ServiceContext serviceContext = ServiceContextFactory.getInstance(
			DDMFormInstanceRecord.class.getName(), resourceRequest);

		serviceContext.setAttribute("status", WorkflowConstants.STATUS_DRAFT);
		serviceContext.setAttribute("validateDDMFormValues", Boolean.FALSE);
		serviceContext.setWorkflowAction(WorkflowConstants.ACTION_SAVE_DRAFT);

		return serviceContext;
	}

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		String formInstanceUuid = ParamUtil.getString(
			resourceRequest, "formInstanceUuid",
			FormsChatbotWebWebKeys.FORMS_UUID);

		DDMFormInstance formInstance =
			ddmFormInstanceLocalService.fetchDDMFormInstanceByUuidAndGroupId(
				formInstanceUuid, portal.getScopeGroupId(resourceRequest));

		DDMStructure structure = formInstance.getStructure();

		DDMForm ddmForm = structure.getDDMForm();

		DDMFormInstanceRecordVersion ddmFormInstanceRecordVersion =
			ddmFormInstanceRecordVersionLocalService.
				fetchLatestFormInstanceRecordVersion(
					portal.getUserId(resourceRequest),
					formInstance.getFormInstanceId(), formInstance.getVersion(),
					WorkflowConstants.STATUS_DRAFT);

		long userId = portal.getUserId(resourceRequest);

		if (Validator.isNull(
				ParamUtil.getString(resourceRequest, "fieldName")) &&
			(ddmFormInstanceRecordVersion != null)) {

			ddmFormInstanceRecordLocalService.updateStatus(
				userId,
				ddmFormInstanceRecordVersion.getFormInstanceRecordVersionId(),
				WorkflowConstants.STATUS_APPROVED,
				ServiceContextFactory.getInstance(
					DDMFormInstanceRecord.class.getName(), resourceRequest));
		}
		else {
			DDMFormValues ddmFormValues = createDDMFormValues(
				resourceRequest, ddmForm, ddmFormInstanceRecordVersion);

			ServiceContext serviceContext = createServiceContext(
				resourceRequest);

			if (ddmFormInstanceRecordVersion == null) {
				ddmFormInstanceRecordLocalService.addFormInstanceRecord(
					userId, formInstance.getGroupId(),
					formInstance.getFormInstanceId(), ddmFormValues,
					serviceContext);
			}
			else {
				ddmFormInstanceRecordLocalService.updateFormInstanceRecord(
					userId,
					ddmFormInstanceRecordVersion.getFormInstanceRecordId(),
					false, ddmFormValues, serviceContext);
			}
		}
	}

	@Reference
	protected DDMFormInstanceLocalService ddmFormInstanceLocalService;

	@Reference
	protected DDMFormInstanceRecordLocalService
		ddmFormInstanceRecordLocalService;

	@Reference
	protected DDMFormInstanceRecordVersionLocalService
		ddmFormInstanceRecordVersionLocalService;

	@Reference
	protected DDMFormValuesMerger ddmFormValuesMerger;

	@Reference
	protected JSONFactory jsonFactory;

	@Reference
	protected Portal portal;

}