import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from 'react-query';
import { ArrowLeft, Calendar, Clock, Save } from 'lucide-react';
import { jobsAPI } from '../services/api';
import toast from 'react-hot-toast';

const jobTypes = [
  { value: 'ONE_TIME', label: 'One-time Job', description: 'Execute once at a specific time' },
  { value: 'RECURRING', label: 'Recurring Job', description: 'Execute based on a schedule' },
  { value: 'BATCH', label: 'Batch Job', description: 'Process multiple items together' },
];

const priorityOptions = [
  { value: 0, label: 'Low' },
  { value: 1, label: 'Normal' },
  { value: 2, label: 'High' },
  { value: 3, label: 'Critical' },
];

function CreateJob() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    jobType: 'ONE_TIME',
    payload: '',
    priority: 1,
    maxRetries: 3,
    scheduledAt: '',
    cronExpression: '',
  });

  const [errors, setErrors] = useState({});

  const createJobMutation = useMutation(
    (jobData) => jobsAPI.createJob(jobData),
    {
      onSuccess: (response) => {
        queryClient.invalidateQueries('jobs');
        toast.success('Job created successfully!');
        navigate(`/jobs/${response.data.id}`);
      },
      onError: (error) => {
        const errorMessage = error.response?.data?.message || 'Failed to create job';
        toast.error(errorMessage);
        setErrors({ general: errorMessage });
      },
    }
  );

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Job name is required';
    }

    if (formData.jobType === 'ONE_TIME' && !formData.scheduledAt) {
      newErrors.scheduledAt = 'Scheduled time is required for one-time jobs';
    }

    if (formData.jobType === 'RECURRING' && !formData.cronExpression.trim()) {
      newErrors.cronExpression = 'Cron expression is required for recurring jobs';
    }

    if (formData.maxRetries < 0 || formData.maxRetries > 10) {
      newErrors.maxRetries = 'Max retries must be between 0 and 10';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    const jobData = {
      name: formData.name,
      description: formData.description,
      jobType: formData.jobType,
      payload: formData.payload || null,
      priority: parseInt(formData.priority),
      maxRetries: parseInt(formData.maxRetries),
    };

    if (formData.jobType === 'ONE_TIME') {
      jobData.scheduledAt = formData.scheduledAt;
    } else if (formData.jobType === 'RECURRING') {
      jobData.cronExpression = formData.cronExpression;
    }

    createJobMutation.mutate(jobData);
  };

  const getMinDateTime = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 1); // At least 1 minute in the future
    return now.toISOString().slice(0, 16);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/jobs')}
          className="p-2 text-gray-400 hover:text-gray-600"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Create New Job</h1>
          <p className="mt-1 text-sm text-gray-500">
            Schedule a new job to be executed by the system
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          {/* Basic Information */}
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-medium text-gray-900">Basic Information</h3>
            </div>
            <div className="card-body space-y-4">
              <div>
                <label className="form-label">Job Name *</label>
                <input
                  type="text"
                  name="name"
                  className={`form-input ${errors.name ? 'border-red-300' : ''}`}
                  placeholder="Enter job name"
                  value={formData.name}
                  onChange={handleChange}
                />
                {errors.name && <p className="form-error">{errors.name}</p>}
              </div>

              <div>
                <label className="form-label">Description</label>
                <textarea
                  name="description"
                  rows={3}
                  className="form-input"
                  placeholder="Enter job description"
                  value={formData.description}
                  onChange={handleChange}
                />
              </div>

              <div>
                <label className="form-label">Job Type *</label>
                <div className="space-y-2">
                  {jobTypes.map((type) => (
                    <label key={type.value} className="flex items-start">
                      <input
                        type="radio"
                        name="jobType"
                        value={type.value}
                        checked={formData.jobType === type.value}
                        onChange={handleChange}
                        className="mt-1 h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300"
                      />
                      <div className="ml-3">
                        <div className="text-sm font-medium text-gray-900">{type.label}</div>
                        <div className="text-sm text-gray-500">{type.description}</div>
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <label className="form-label">Priority</label>
                <select
                  name="priority"
                  className="form-input"
                  value={formData.priority}
                  onChange={handleChange}
                >
                  {priorityOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="form-label">Max Retries</label>
                <input
                  type="number"
                  name="maxRetries"
                  min="0"
                  max="10"
                  className={`form-input ${errors.maxRetries ? 'border-red-300' : ''}`}
                  value={formData.maxRetries}
                  onChange={handleChange}
                />
                {errors.maxRetries && <p className="form-error">{errors.maxRetries}</p>}
              </div>
            </div>
          </div>

          {/* Scheduling */}
          <div className="card">
            <div className="card-header">
              <h3 className="text-lg font-medium text-gray-900">Scheduling</h3>
            </div>
            <div className="card-body space-y-4">
              {formData.jobType === 'ONE_TIME' && (
                <div>
                  <label className="form-label">Scheduled Time *</label>
                  <div className="relative">
                    <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <input
                      type="datetime-local"
                      name="scheduledAt"
                      className={`form-input pl-10 ${errors.scheduledAt ? 'border-red-300' : ''}`}
                      min={getMinDateTime()}
                      value={formData.scheduledAt}
                      onChange={handleChange}
                    />
                  </div>
                  {errors.scheduledAt && <p className="form-error">{errors.scheduledAt}</p>}
                </div>
              )}

              {formData.jobType === 'RECURRING' && (
                <div>
                  <label className="form-label">Cron Expression *</label>
                  <div className="relative">
                    <Clock className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                    <input
                      type="text"
                      name="cronExpression"
                      className={`form-input pl-10 ${errors.cronExpression ? 'border-red-300' : ''}`}
                      placeholder="0 0 9 * * ? (Daily at 9 AM)"
                      value={formData.cronExpression}
                      onChange={handleChange}
                    />
                  </div>
                  {errors.cronExpression && <p className="form-error">{errors.cronExpression}</p>}
                  <p className="mt-1 text-sm text-gray-500">
                    Use standard cron format. Examples: "0 0 9 * * ?" (daily at 9 AM), "0 0 0 1 * ?" (monthly)
                  </p>
                </div>
              )}

              {formData.jobType === 'BATCH' && (
                <div className="bg-blue-50 p-4 rounded-lg">
                  <div className="flex">
                    <div className="flex-shrink-0">
                      <Calendar className="h-5 w-5 text-blue-400" />
                    </div>
                    <div className="ml-3">
                      <h3 className="text-sm font-medium text-blue-800">Batch Job</h3>
                      <p className="mt-1 text-sm text-blue-700">
                        Batch jobs are executed immediately when created. Use the payload field to specify the items to process.
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Payload */}
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">Job Payload</h3>
          </div>
          <div className="card-body">
            <div>
              <label className="form-label">Payload (JSON)</label>
              <textarea
                name="payload"
                rows={6}
                className="form-input font-mono text-sm"
                placeholder='{"data": "example", "config": {"timeout": 30000}}'
                value={formData.payload}
                onChange={handleChange}
              />
              <p className="mt-1 text-sm text-gray-500">
                Optional JSON payload that will be passed to the job execution context
              </p>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => navigate('/jobs')}
            className="btn btn-secondary"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={createJobMutation.isLoading}
            className="btn btn-primary flex items-center"
          >
            {createJobMutation.isLoading ? (
              <div className="loading-spinner mr-2"></div>
            ) : (
              <Save className="h-4 w-4 mr-2" />
            )}
            Create Job
          </button>
        </div>

        {errors.general && (
          <div className="bg-red-50 border border-red-200 rounded-md p-4">
            <p className="text-sm text-red-600">{errors.general}</p>
          </div>
        )}
      </form>
    </div>
  );
}

export default CreateJob;
