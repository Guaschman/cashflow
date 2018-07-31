from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods

from expenses.models import Expense
from expenses.models import File


def pretty_request(request):
    headers = ''
    for header, value in request.META.items():
        if not header.startswith('HTTP'):
            continue
        header = '-'.join([h.capitalize() for h in header[5:].lower().split('_')])
        headers += '{}: {}\n'.format(header, value)

    return (
        '{method} HTTP/1.1\n'
        'Content-Length: {content_length}\n'
        'Content-Type: {content_type}\n'
        '{headers}\n\n'
        # '{body}'
    ).format(
        method=request.method,
        content_length=request.META['CONTENT_LENGTH'],
        content_type=request.META['CONTENT_TYPE'],
        headers=headers,
        # body=request.body,
    )


@require_http_methods(["POST"])
@csrf_exempt
def new_file(request):
    if len((request.FILES.getlist('files'))) < 1:
        return JsonResponse({'message': 'No file specified.', 'explanation': 'Upload at least one file.'}, status=400)

    expense_id = int(request.GET.get('expense', '0'))
    expense = None
    if expense_id > 0:
        expense = Expense.objects.get(pk=expense_id)

    # Upload the file
    files = []
    for uploaded_file in request.FILES.getlist('files'):
        file = File(file=uploaded_file, expense=expense)
        file.save()

        files.append(file)

    print(files)

    return JsonResponse({'message': 'File uploaded.', 'files': [file.to_dict() for file in files]})


@require_http_methods(["POST"])
@csrf_exempt
def delete_file(request, pk):
    file = File.objects.get(pk=int(pk))
    if file.expense is not None and not request.user.profile.may_delete(file.expense):
        return JsonResponse({'Du har inte behörighet att ta bort denna bild.'}, 403)
    file.expense = None
    file.save()

    return JsonResponse({'message': 'File deleted.'})
