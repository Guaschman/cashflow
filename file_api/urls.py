from django.conf.urls import url

import file_api.views as views

urlpatterns = [
    url(r'^new/$', views.new_file, name='file_api-new'),
    url(r'^(?P<pk>\d+)/delete/$', views.delete_file, name='file_api-delete'),
]
