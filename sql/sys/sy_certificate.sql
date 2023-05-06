/* sy_certificate_type */
-- :name list-sy_certificate_type :? :* :D
select * from sy_certificate_type sct
/*~ where sct.del_flag = '0' [and sct.certificate_type_name like :l:certificate_type_name]
[and date_format(sct.create_time, '%Y-%m-%d') >= :beginTime] [and date_format(sct.create_time, '%Y-%m-%d') <= :endTime]
[and sct.parent_id = :parent_id]
~*/
order by sct.sort
--~ [limit :limit offset :offset]

-- :name get-certificateType-ancestors :? :1
select ancestors from sy_certificate_type where id = :certificate_id

-- :name list-sy_certificate_type-count :? :1 :D
select count(1) as cnt from sy_certificate_type sct
/*~ where sct.del_flag = '0' [and sct.certificate_type_name like :l:certificate_type_name]
[and date_format(sct.create_time, '%Y-%m-%d') >= :beginTime] [and date_format(sct.create_time, '%Y-%m-%d') <= :endTime]
~*/

/* sy_certificate */
-- :name list-sy_certificate :? :* :D
select * from (select sc.id, sc.user_id, su.user_name, su.nick_name, sd.dept_name, sc.issue, sc.first_date, sct.certificate_type_name,
              sc.certificate_no, sc.registration_no, sc.qualification_certificate_no, sc.profession_type,
              sc.is_limit, sc.limit_start_time, sc.limit_end_time, sc.review_time, sc.project_no, sc.address,
              sc.status, sc.create_by, sc.create_time, sc.update_by, sc.update_time, sc.remarks, sc.del_flag,
              (select (case when datediff(limit_end_time, NOW()) < 0 then '3'
                        when datediff(limit_end_time, NOW()) >= 0 and datediff(limit_end_time, NOW()) <= 90 then '2'
                        else '1' end)) as alert_status from sy_certificate sc left join sys_dept sd on sc.dept_id = sd.dept_id
                          left join sys_user su on sc.user_id = su.user_id
                          left join sy_certificate_type sct on sc.certificate_type_id = sct.id) t
/*~
where t.del_flag = '0' [and t.nick_name like :l:nick_name] [and t.certificate_no like :l:certificate_no]
[and t.alert_status = :alert_status] [and t.user_id = :user_id]
~*/
order by t.create_time desc
--~ [limit :limit offset :offset]

-- :name list-sy_certificate-count :? :1 :D
select count(1) as cnt from
   (select sc.user_id, su.nick_name, sc.dept_id, sc.certificate_no, sc.del_flag, (select (case when datediff(limit_end_time, NOW()) < 0 then '3'
       when datediff(limit_end_time, NOW()) >= 0 and datediff(limit_end_time, NOW()) <= 90 then '2'
       else '1' end)) as alert_status from sy_certificate sc left join sys_user su on sc.user_id = su.user_id) t
--~ where t.del_flag = '0' [and t.user_id = :user_id] [and t.nick_name like :l:nick_name] [and t.alert_status = :alert_status]

-- :name get-detail-sy_certificate :? :1
select sc.id, su.nick_name, sd.dept_name, sc.issue, sc.first_date, sct.certificate_type_name, sc.certificate_no,
sc.registration_no, sc.qualification_certificate_no, sc.profession_type, sc.is_limit, sc.limit_start_time,
sc.limit_end_time, sc.review_time, sc.project_no, sc.address, sc.status, sc.appendix, u.nick_name as create_by,
sc.create_time, sc.remarks from sy_certificate sc
left join sys_dept sd on sc.dept_id = sd.dept_id
left join sys_user su on sc.user_id = su.user_id
left join sys_user u on sc.create_by = u.user_id
left join sy_certificate_type sct on sct.id = sc.certificate_type_id
where sc.id = :id

-- :name tree-sy_certificate_type :? :*
select id as id, certificate_type_name as label from sy_certificate_type
-- where del_flag = '0'
order by id

-- :name list-sy_certificate-detail :? :* :D
select sc.id,sc.dept_id,sc.issue,sc.first_date,sc.certificate_type_id,sc.certificate_no,sc.registration_no,
sc.qualification_certificate_no,sc.profession_type,sc.is_limit,sc.limit_start_time,sc.limit_end_time,
sc.review_time,sc.project_no,sc.address,sc.`status`,sc.appendix,sc.del_flag,sc.create_by,sc.create_time,
sc.update_by,sc.update_time,sc.remarks,sct.certificate_type_name,su.number as user_id
from sy_certificate sc
left join sy_certificate_type sct on sct.id = sc.certificate_type_id
left join sys_user su on sc.user_id=su.user_id
--~ where sc.del_flag = 0 [and sc.user_id = :user_id] [and date_format(sc.create_time, '%Y-%m-%d') >= :start_date] [and date_format(sc.create_time, '%Y-%m-%d') <= :end_date]
order by sc.id

-- :name list-sy_certificate-detail-count :? :1 :D
select count(1) as cnt from sy_certificate
--~ where del_flag = 0 [and user_id = :user_id] [and date_format(create_time, '%Y-%m-%d') >= :start_date] [and date_format(create_time, '%Y-%m-%d') <= :end_date]

-- :name get-cert-type-by-name :? :*
select id from sy_certificate_type where certificate_type_name = :name

-- :name get-ce-cert-type-by-name :? :*
select id from ce_certificate_type where name = :name and parent_id = 0

-- :name get-cert-parent-by-name :? :*
select id from ce_certificate_type where name = :name and parent_id = :parent_id

-- :name list-user-certificate :? :*
select sc.id, sct.certificate_type_name, sc.certificate_no, sc.profession_type from sy_certificate sc
left join sy_certificate_type sct on sc.certificate_type_id = sct.id
where sc.limit_end_time > now() and sc.user_id = :user_id order by sc.first_date

-- :name list-ce_external_info :? :* :D
select so.*, su.nick_name, su.number, su.idcard_number, su.phonenumber, sd.dept_name, sp.post_name,ct.name as cert_id,
       ct_a.name as cert_name, ct_b.name as cert_level from ce_external_info so
left join sys_user su on so.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on so.cert_id = ct.id
left join ce_certificate_type ct_a on so.cert_name = ct_a.id
left join ce_certificate_type ct_b on so.cert_level = ct_b.id
/*~
where so.del_flag = '0' [and su.number like :l:number] [and su.nick_name like :l:nick_name] [and so.cert_status like :l:cert_status]
[and sd.dept_name like :l:dept_name] [and sd.dept_id = :dept_id] [and sp.post_name like :l:post_name] [and sp.post_id like :l:post_id] [and ct_a.name like :l:cert_name]
~*/
order by so.create_time desc
--~ [limit :limit offset :offset]

-- :name list-ce_external_info-count :? :1 :D
select count(1) as cnt from ce_external_info so
left join sys_user su on so.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on so.cert_id = ct.id
left join ce_certificate_type ct_a on so.cert_name = ct_a.id
left join ce_certificate_type ct_b on so.cert_level = ct_b.id
/*~
where so.del_flag = '0' [and su.number like :l:number] [and su.nick_name like :l:nick_name] [and so.cert_status like :l:cert_status]
[and sd.dept_name like :l:dept_name] [and sd.dept_id = :dept_id] [and sp.post_name like :l:post_name] [and sp.post_id like :l:post_id] [and ct_a.name like :l:cert_name]
~*/

-- :name get-detail-ce_external_info :? :1
select so.*, su.nick_name, su.number, su.idcard_number, su.phonenumber, sd.dept_name, sp.post_name, ct.name as cert_id,
        ct_a.name as cert_name, ct_b.name as cert_level from ce_external_info so
left join sys_user su on so.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on so.cert_id = ct.id
left join ce_certificate_type ct_a on so.cert_name = ct_a.id
left join ce_certificate_type ct_b on so.cert_level = ct_b.id
where so.id= :id

-- :name get-external_info :? :1
select so.*, su.nick_name, su.number, su.idcard_number, su.phonenumber, sd.dept_name, sp.post_name from ce_external_info so
left join sys_user su on so.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
where so.id= :id

-- :name get-detail-outer_certificate_appendix :? :1
select appendix from ce_external_info
where id = :id

-- :name get-ce_skill_certificate :? :1
select ce.id, ce.user_id, ce.cert_id, ce.job_title, ce.cert_level, ce.type_work, ce.cert_code, ce.opening_date, ce.cer_authority, ce.appendix, ce.remark, su.nick_name, su.number, sd.dept_name,
       sp.post_name  from ce_skill_level_certificate ce
left join sys_user su on ce.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
where ce.id = :id

-- :name list-ce_skill_level_certificate :? :* :D
select  ce.id, ce.user_id, ce.cert_code, ce.opening_date, ce.cer_authority, ce.appendix, ce.remark, su.nick_name, su.number, sd.dept_name,
       sp.post_name, ct.name as cert_id, ct_a.name as job_title, ct_b.name as cert_level, ct_c.name as type_work  from ce_skill_level_certificate ce
left join sys_user su on ce.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on ce.cert_id = ct.id
left join ce_certificate_type ct_a on ce.job_title = ct_a.id
left join ce_certificate_type ct_b on ce.cert_level = ct_b.id
left join ce_certificate_type ct_c on ce.type_work = ct_c.id
/*~
where ce.del_flag = '0' [and su.number like :l:number] [and su.nick_name like :l:nick_name]
[and sd.dept_name like :l:dept_name] [and sd.dept_id = :dept_id] [and sp.post_name like :l:post_name] [and sp.post_id like :l:post_id]
~*/
order by ce.create_time desc
--~ [limit :limit offset :offset]

-- :name list-ce_skill_level_certificate-count :? :1 :D
select count(1) as cnt from ce_skill_level_certificate ce
left join sys_user su on ce.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on ce.cert_id = ct.id
left join ce_certificate_type ct_a on ce.job_title = ct_a.id
left join ce_certificate_type ct_b on ce.cert_level = ct_b.id
left join ce_certificate_type ct_c on ce.type_work = ct_c.id
/*~
where ce.del_flag = '0' [and su.number like :l:number] [and su.nick_name like :l:nick_name]
[and sd.dept_name like :l:dept_name] [and sd.dept_id = :dept_id] [and sp.post_name like :l:post_name] [and sp.post_id like :l:post_id]
~*/

-- :name get-detail-kill_level_certificate :? :1
select  ce.id, ce.user_id, ce.cert_code, ce.opening_date, ce.cer_authority, ce.appendix, ce.remark, su.nick_name, su.number, sd.dept_name,
       sp.post_name, ct.name as cert_id, ct_a.name as job_title, ct_b.name as cert_level, ct_c.name as type_work  from ce_skill_level_certificate ce
left join sys_user su on ce.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on ce.cert_id = ct.id
left join ce_certificate_type ct_a on ce.job_title = ct_a.id
left join ce_certificate_type ct_b on ce.cert_level = ct_b.id
left join ce_certificate_type ct_c on ce.type_work = ct_c.id
where ce.id = :id

-- :name list-ce_examiner_certificate :? :* :D
select cec.update_time, su.nick_name, su.number, sd.dept_id, sd.dept_name, sp.post_name, cec.cert_code,
cec.cert_level, cec.engage_date, cec.dismissal_date, cec.location, cec.status, cec.appendix, cec.remark
from ce_examiner_certificate cec
left join sys_user su on cec.user_id=su.user_id
left join sys_dept sd on su.dept_id=sd.dept_id
left join sys_user_post sup on su.user_id=sup.user_id
left join sys_post sp on sup.post_id=sp.post_id
--~ where cec.del_flag= '0' [and su.number = :number] [and su.nick_name = :nick_name] [and sd.dept_id = :dept_id]
order by cec.create_time desc
--~ [limit :limit offset :offset]

-- :name list-ce_examiner_certificate-count :? :1 :D
select count(1) as cnt from ce_examiner_certificate cec
left join sys_user su on cec.user_id=su.user_id
left join sys_dept sd on su.dept_id=sd.dept_id
left join sys_user_post sup on su.user_id=sup.user_id
left join sys_post sp on sup.post_id=sp.post_id
--~ where cec.del_flag = '0' [and su.number = :number] [and su.nick_name = :nick_name] [and sd.dept_id = :dept_id]

-- :name list-ce_certificate_type :? :* :D
select * from ce_certificate_type ce
/*~ where ce.del_flag = '0' [and ce.name like :l:name]
[and date_format(ce.create_time, '%Y-%m-%d') >= :beginTime] [and date_format(ce.create_time, '%Y-%m-%d') <= :endTime]
~*/
order by ce.sort
--~ [limit :limit offset :offset]

-- :name list-ce_certificate_type-count :? :1 :D
select count(1) as cnt from ce_certificate_type ce
/*~ where ce.del_flag = '0' [and ce.name like :l:name]
[and date_format(ce.create_time, '%Y-%m-%d') >= :beginTime] [and date_format(ce.create_time, '%Y-%m-%d') <= :endTime]
~*/

-- :name get-ce_certificate_type-ancestors :? :1
select ancestors from ce_certificate_type where id = :certificate_id

-- :name list-certificate_type_first :? :* :D
select id, name from ce_certificate_type
--~ where del_flag = 0 [and parent_id = :parent_id]

-- :name list-certificate_type_children :? :* :D
select id, name from ce_certificate_type
--~ where del_flag = 0 [and parent_id = :parent_id]

-- :name get-sy_certificate_type-id :? :*
select id from sy_certificate_type where del_flag='0' and certificate_type_name = :certificate_type_name

-- :name get-cert-typeId-by-name :? :*
select id from ce_certificate_type where name = :name

-- :name list-ce_course_completion_certificate :? :* :D
select  cc.id, cc.user_id, cc.cert_code, cc.opening_date, cc.cert_authority, cc.cert_type, cc.appendix, cc.remark, su.nick_name, su.number, sd.dept_name,
        sp.post_name, ct.name as cert_id, ct_a.name as cert_training_name, ct_b.name as cert_major, ct_c.name as cert_level  from ce_course_completion_certificate cc
left join sys_user su on cc.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on cc.cert_id = ct.id
left join ce_certificate_type ct_a on cc.cert_training_name = ct_a.id
left join ce_certificate_type ct_b on cc.cert_major = ct_b.id
left join ce_certificate_type ct_c on cc.cert_level = ct_c.id
/*~
where cc.del_flag = '0' [and su.number like :l:number] [and su.nick_name like :l:nick_name] [and cc.cert_type like :l:cert_type]
[and sd.dept_name like :l:dept_name] [and sd.dept_id = :dept_id] [and sp.post_name like :l:post_name] [and sp.post_id like :l:post_id]
~*/
order by cc.create_time desc
--~ [limit :limit offset :offset]

-- :name list-ce_course_completion_certificate-count :? :1 :D
select count(1) as cnt from ce_course_completion_certificate cc
left join sys_user su on cc.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on cc.cert_id = ct.id
left join ce_certificate_type ct_a on cc.cert_training_name = ct_a.id
left join ce_certificate_type ct_b on cc.cert_major = ct_b.id
left join ce_certificate_type ct_c on cc.cert_level = ct_c.id
/*~
where cc.del_flag = '0' [and su.number like :l:number] [and su.nick_name like :l:nick_name] [and cc.cert_type like :l:cert_type]
[and sd.dept_name like :l:dept_name] [and sd.dept_id = :dept_id] [and sp.post_name like :l:post_name] [and sp.post_id like :l:post_id]
~*/

-- :name get-detail-ce_course_completion_certificate :? :1
select  cc.id, cc.user_id, cc.cert_code, cc.opening_date, cc.cert_authority, cc.cert_type, cc.appendix, cc.remark, su.nick_name, su.number, sd.dept_name,
        sp.post_name, ct.name as cert_id, ct_a.name as cert_training_name, ct_b.name as cert_major, ct_c.name as cert_level  from ce_course_completion_certificate cc
left join sys_user su on cc.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
left join ce_certificate_type ct on cc.cert_id = ct.id
left join ce_certificate_type ct_a on cc.cert_training_name = ct_a.id
left join ce_certificate_type ct_b on cc.cert_major = ct_b.id
left join ce_certificate_type ct_c on cc.cert_level = ct_c.id
where cc.id = :id

-- :name get-course_completion :? :1
select  cc.id, cc.user_id, cc.cert_code, cc.cert_id, cc.cert_training_name, cc.cert_level, cc.cert_major, cc.opening_date,
       cc.cert_authority, cc.cert_type,su.nick_name, su.number, sd.dept_name,
sp.post_name from ce_course_completion_certificate cc
left join sys_user su on cc.user_id = su.user_id
left join sys_dept sd on su.dept_id = sd.dept_id
left join sys_user_post sup on su.user_id = sup.user_id
left join sys_post sp on sup.post_id = sp.post_id
where cc.id= :id