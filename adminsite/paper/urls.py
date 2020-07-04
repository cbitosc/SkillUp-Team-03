from django.urls import path
from . import views

app_name='paper'
urlpatterns=[
path('',views.login,name='login'),
path('home/',views.home,name='home'),
path('paperuploadform/',views.adminpaperpage,name='adminpaperpage'),
path('timetableform/',views.admintimetablepage,name='admintimetablepage'),
path('edittableform/',views.edittimetablepage,name='edittimetablepage'),
path('uploadpaper/',views.uploadpaper,name='uploadpaper'),
path('timetable/',views.timetable,name='timetable'),
path('edittable/',views.edittable,name='edittable'),
path('sendedittable/',views.sendedittable,name='sendedittable'),
path('deletetimetable/',views.deletetimetable,name='deletetimetable'),
path('check_user_upload/',views.check_user_upload,name='check_user_upload'),
path('getrequestnoinfo/',views.getrequestnoinfo,name='getrequestnoinfo'),
path('sendrequestinfo/',views.sendrequestinfo,name='sendrequestinfo'),
path('redirectimage/',views.redirectimage,name='redirectimage'),
]
