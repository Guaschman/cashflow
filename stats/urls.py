from django.conf.urls import url

import stats.views as views

urlpatterns = [
    url(r'^$', views.index, name='stats-index'),
]
